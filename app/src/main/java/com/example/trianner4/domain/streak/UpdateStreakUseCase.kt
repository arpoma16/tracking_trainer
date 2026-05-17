package com.example.trianner4.domain.streak

import com.example.trianner4.data.local.PlannedSessionStatus
import com.example.trianner4.data.local.dao.PlannedSessionDao
import com.example.trianner4.data.local.dao.SessionDao
import com.example.trianner4.data.local.dao.StreakStateDao
import com.example.trianner4.data.local.dao.UserProfileDao
import com.example.trianner4.data.local.entity.PlannedSessionEntity
import com.example.trianner4.data.local.entity.StreakStateEntity
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recalculates both streak metrics and persists them to [StreakStateEntity].
 *
 * Also handles the non-destructive roll-forward: any PENDING planned session whose
 * plannedDate is strictly before today is marked ROLLED_FORWARD, and a new PENDING is
 * inserted for the following day. This preserves the routine adherence streak even when
 * the user trains one day later than originally scheduled.
 *
 * Call on app launch (via TodayViewModel) and after every session close
 * (via SesionActivaViewModel) to keep streak counts up-to-date.
 */
@Singleton
class UpdateStreakUseCase @Inject constructor(
    private val plannedSessionDao: PlannedSessionDao,
    private val sessionDao: SessionDao,
    private val streakStateDao: StreakStateDao,
    private val userProfileDao: UserProfileDao
) {

    suspend operator fun invoke() {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val todayEpoch = today.atStartOfDay(zone).toInstant().toEpochMilli()

        rollForwardOverdue(today, zone, todayEpoch)

        val weeklyTarget = userProfileDao.get()?.weeklyTargetSessions ?: DEFAULT_WEEKLY_TARGET
        val weeklyCount = computeWeeklyConsistencyStreak(today, zone, weeklyTarget)
        val adherenceCount = computeRoutineAdherenceStreak(todayEpoch)

        val anchorWeek = today.get(WeekFields.ISO.weekOfWeekBasedYear())
        val now = System.currentTimeMillis()

        val existing = streakStateDao.get()
        if (existing == null) {
            streakStateDao.upsert(
                StreakStateEntity(
                    weeklyConsistencyCount = weeklyCount,
                    weeklyConsistencyAnchorWeek = anchorWeek,
                    routineAdherenceCount = adherenceCount,
                    routineAdherenceAnchorDate = todayEpoch,
                    lastUpdated = now
                )
            )
        } else {
            streakStateDao.updateCounts(
                weeklyCount = weeklyCount,
                anchorWeek = anchorWeek,
                adherenceCount = adherenceCount,
                adherenceAnchor = todayEpoch,
                updatedAt = now
            )
        }
    }

    // ── Roll-forward ───────────────────────────────────────────────────────────

    /**
     * For every overdue PENDING entry (plannedDate < today), marks it ROLLED_FORWARD
     * and inserts a new PENDING for the next calendar day.
     *
     * Processing is done oldest-first so that a multi-day absence cascades naturally:
     * Day-1 → rolls to Day-2, Day-2 → rolls to Day-3, …, eventually landing on today.
     * The INSERT OR IGNORE ensures that if a PENDING already exists for the target date
     * (e.g. from MaterializePlannedSessionsUseCase), no duplicate is created.
     */
    private suspend fun rollForwardOverdue(today: LocalDate, zone: ZoneId, todayEpoch: Long) {
        val overdue = plannedSessionDao.getOverduePending(todayEpoch)
        for (planned in overdue) {
            plannedSessionDao.markRolledForward(planned.id)

            val originalPlannedDate = Instant.ofEpochMilli(planned.plannedDate)
                .atZone(zone)
                .toLocalDate()
            val nextDate = originalPlannedDate.plusDays(1)
            val nextDateEpoch = nextDate.atStartOfDay(zone).toInstant().toEpochMilli()

            plannedSessionDao.insert(
                PlannedSessionEntity(
                    routineId = planned.routineId,
                    plannedDate = nextDateEpoch,
                    status = PlannedSessionStatus.PENDING,
                    // Preserve the root obligation date across the entire roll chain
                    originalDate = planned.originalDate ?: planned.plannedDate
                )
            )
        }
    }

    // ── Racha de Consistencia Semanal ──────────────────────────────────────────

    /**
     * Counts consecutive ISO weeks (going backwards from the current week) in which the
     * user completed at least [weeklyTarget] sessions.
     *
     * The current week is exempt from breaking the streak if it hasn't yet met the target
     * (it may still be in progress). Once a past week is found with fewer sessions than
     * the target, the streak stops.
     */
    private suspend fun computeWeeklyConsistencyStreak(
        today: LocalDate,
        zone: ZoneId,
        weeklyTarget: Int
    ): Int {
        // Anchor to Monday of the current ISO week
        var weekStart = today.with(DayOfWeek.MONDAY).let {
            if (it.isAfter(today)) it.minusWeeks(1) else it
        }

        var streak = 0
        repeat(MAX_WEEKS_LOOKBACK) { iteration ->
            val weekEnd = weekStart.plusDays(6)
            val startEpoch = weekStart.atStartOfDay(zone).toInstant().toEpochMilli()
            // endEpoch = start of the day AFTER weekEnd (exclusive upper bound)
            val endEpoch = weekEnd.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

            val count = sessionDao.countCompletedInRange(startEpoch, endEpoch)
            val isCurrentWeek = iteration == 0

            when {
                count >= weeklyTarget -> streak++
                isCurrentWeek -> { /* week still in progress — don't penalize */ }
                else -> return streak
            }
            weekStart = weekStart.minusWeeks(1)
        }
        return streak
    }

    // ── Racha de Adherencia a la Rutina ────────────────────────────────────────

    /**
     * Counts consecutive COMPLETED planned sessions going backwards from today.
     *
     * Rules:
     * - COMPLETED   → +1 to streak
     * - ROLLED_FORWARD → excluded from query (filtered at DB level); streak continues
     * - PENDING on today → still possible; skip without breaking
     * - PENDING with date < today → overdue miss; streak stops (returns current count)
     * - SKIPPED → streak stops
     */
    private suspend fun computeRoutineAdherenceStreak(todayEpoch: Long): Int {
        val recent = plannedSessionDao.getRecentForAdherenceStreak(
            todayEpoch = todayEpoch,
            limit = MAX_ADHERENCE_LOOKBACK
        )
        var streak = 0
        for (planned in recent) {
            when (planned.status) {
                PlannedSessionStatus.COMPLETED -> streak++
                PlannedSessionStatus.ROLLED_FORWARD -> continue   // neutral (excluded by DB query; guard here)
                PlannedSessionStatus.PENDING -> {
                    if (planned.plannedDate < todayEpoch) return streak   // overdue → miss
                    // plannedDate == today: still possible, skip without breaking
                }
                PlannedSessionStatus.SKIPPED -> return streak
            }
        }
        return streak
    }

    companion object {
        private const val DEFAULT_WEEKLY_TARGET = 3
        private const val MAX_WEEKS_LOOKBACK = 52
        private const val MAX_ADHERENCE_LOOKBACK = 60
    }
}
