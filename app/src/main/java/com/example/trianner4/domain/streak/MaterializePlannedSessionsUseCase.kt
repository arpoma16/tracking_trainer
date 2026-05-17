package com.example.trianner4.domain.streak

import com.example.trianner4.data.local.ScheduleType
import com.example.trianner4.data.local.dao.PlannedSessionDao
import com.example.trianner4.data.local.dao.RoutineDao
import com.example.trianner4.data.local.entity.PlannedSessionEntity
import com.example.trianner4.data.local.entity.RoutineScheduleEntity
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Converts recurring [RoutineScheduleEntity] rules into concrete [PlannedSessionEntity] rows
 * for the next [daysAhead] days. Idempotent: rows already in the database are silently skipped
 * (INSERT OR IGNORE + UNIQUE index on routineId+plannedDate).
 *
 * Call on app launch and whenever a routine's schedule changes.
 */
@Singleton
class MaterializePlannedSessionsUseCase @Inject constructor(
    private val routineDao: RoutineDao,
    private val plannedSessionDao: PlannedSessionDao
) {

    suspend operator fun invoke(daysAhead: Int = 14) {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val batch = mutableListOf<PlannedSessionEntity>()

        val routines = routineDao.observeActive().first()
        for (routine in routines) {
            val schedules = routineDao.getSchedulesForRoutine(routine.id)
            for (offset in 0 until daysAhead) {
                val date = today.plusDays(offset.toLong())
                if (schedules.any { isScheduledForDate(it, date) }) {
                    val dateEpoch = date.atStartOfDay(zone).toInstant().toEpochMilli()
                    batch.add(
                        PlannedSessionEntity(
                            routineId = routine.id,
                            plannedDate = dateEpoch
                        )
                    )
                }
            }
        }

        if (batch.isNotEmpty()) {
            plannedSessionDao.insertAll(batch)
        }
    }

    private fun isScheduledForDate(schedule: RoutineScheduleEntity, date: LocalDate): Boolean =
        when (schedule.scheduleType) {
            ScheduleType.WEEKDAYS -> {
                val mask = schedule.weekdaysMask ?: 0
                // DayOfWeek.value: Mon=1…Sun=7 → bit: Mon=bit0, Sun=bit6
                val bit = 1 shl (date.dayOfWeek.value - 1)
                (mask and bit) != 0
            }
            ScheduleType.EVERY_N_DAYS -> {
                val n = schedule.everyNDays ?: return false
                val anchorMillis = schedule.anchorDate ?: return false
                val anchorDate = Instant.ofEpochMilli(anchorMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val diff = ChronoUnit.DAYS.between(anchorDate, date)
                diff >= 0 && diff % n == 0L
            }
            ScheduleType.CUSTOM -> false
        }
}
