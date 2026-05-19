package com.example.trianner4.domain.today

import com.example.trianner4.data.local.ScheduleType
import com.example.trianner4.data.local.dao.DiscomfortDao
import com.example.trianner4.data.local.dao.ExerciseDao
import com.example.trianner4.data.local.dao.RoutineDao
import com.example.trianner4.data.local.dao.StatusDao
import com.example.trianner4.data.local.dao.StreakStateDao
import com.example.trianner4.data.local.entity.RoutineScheduleEntity
import com.example.trianner4.ui.today.AdaptedPlan
import com.example.trianner4.ui.today.DayStatus
import com.example.trianner4.ui.today.StreakData
import com.example.trianner4.ui.today.TodayRoutineItem
import com.example.trianner4.ui.today.TodayUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodayRepository @Inject constructor(
    private val routineDao: RoutineDao,
    private val discomfortDao: DiscomfortDao,
    private val exerciseDao: ExerciseDao,
    private val streakStateDao: StreakStateDao,
    private val statusDao: StatusDao,
    private val resolver: AdaptationResolver
) {
    /**
     * Flujo reactivo del estado de la pantalla Hoy.
     * Se re-evalúa cada vez que cambian las rutinas activas,
     * las molestias activas o el estado de racha.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeTodayState(): Flow<TodayUiState> =
        combine(
            routineDao.observeActive(),
            discomfortDao.observeActive(),
            streakStateDao.observe()
        ) { routines, discomforts, streak ->
            Triple(routines, discomforts, streak)
        }.flatMapLatest { (routines, discomforts, streak) ->
            flow {
                val today = LocalDate.now()
                val todayEpoch = today
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                val streakData = streak?.let {
                    StreakData(it.weeklyConsistencyCount, it.routineAdherenceCount)
                }

                // Modo Congelación tiene prioridad absoluta
                if (statusDao.getActiveFreeze(todayEpoch) != null) {
                    emit(TodayUiState.NoRoutineToday(DayStatus.FROZEN, streakData))
                    return@flow
                }

                val activeDeload = statusDao.getActiveDeload(todayEpoch)

                // Todas las rutinas activas programadas para hoy
                val todayRoutines = routines.filter { routine ->
                    routineDao.getSchedulesForRoutine(routine.id)
                        .any { isScheduledToday(it, today) }
                }

                if (todayRoutines.isEmpty()) {
                    val status = if (activeDeload != null) DayStatus.DELOAD else DayStatus.REST
                    emit(TodayUiState.NoRoutineToday(status, streakData))
                    return@flow
                }

                val routineItems = mutableListOf<TodayRoutineItem>()
                var overallDayStatus: DayStatus =
                    if (activeDeload != null) DayStatus.DELOAD else DayStatus.TRAINING

                for (routine in todayRoutines) {
                    val routineExercises = routineDao.getPhaseExercisesWithExercise(routine.id)
                    val plan: AdaptedPlan
                    val routineStatus: DayStatus

                    when {
                        discomforts.isNotEmpty() -> {
                            val affected = exerciseDao
                                .getExercisesAffectedByActiveDiscomforts(routine.id)
                            val injectablePre = exerciseDao
                                .getInjectablePreExercises(routine.id)
                            val injectablePost = exerciseDao
                                .getInjectablePostExercises(routine.id)
                            plan = resolver.resolve(
                                routineExercises, discomforts, affected, injectablePre, injectablePost
                            )
                            routineStatus = if (plan.isAdapted) DayStatus.ADAPTED else DayStatus.TRAINING
                        }
                        activeDeload != null -> {
                            val base = resolver.resolve(
                                routineExercises, emptyList(), emptyList(), emptyList(), emptyList()
                            )
                            plan = base.copy(
                                coreExercises = base.coreExercises.map {
                                    it.copy(effectiveLoadFactor = activeDeload.loadFactor)
                                }
                            )
                            routineStatus = DayStatus.DELOAD
                        }
                        else -> {
                            plan = resolver.resolve(
                                routineExercises, emptyList(), emptyList(), emptyList(), emptyList()
                            )
                            routineStatus = DayStatus.TRAINING
                        }
                    }

                    routineItems += TodayRoutineItem(
                        routineId = routine.id,
                        routineName = routine.name,
                        plan = plan,
                    )

                    // ADAPTED > DELOAD > TRAINING for the day-level header
                    if (routineStatus == DayStatus.ADAPTED) overallDayStatus = DayStatus.ADAPTED
                    else if (routineStatus == DayStatus.DELOAD && overallDayStatus == DayStatus.TRAINING)
                        overallDayStatus = DayStatus.DELOAD
                }

                emit(
                    TodayUiState.Ready(
                        routines = routineItems,
                        dayStatus = overallDayStatus,
                        streak = streakData,
                    )
                )
            }
        }

    // ── Lógica de scheduling ───────────────────────────────────────────────────

    private fun isScheduledToday(schedule: RoutineScheduleEntity, today: LocalDate): Boolean =
        when (schedule.scheduleType) {
            ScheduleType.WEEKDAYS -> {
                val mask = schedule.weekdaysMask ?: 0
                // DayOfWeek.value: Mon=1…Sun=7 → bit: Mon=bit0, Sun=bit6
                val bit = 1 shl (today.dayOfWeek.value - 1)
                (mask and bit) != 0
            }
            ScheduleType.EVERY_N_DAYS -> {
                val n = schedule.everyNDays ?: return false
                val anchorMillis = schedule.anchorDate ?: return false
                val anchorDate = Instant.ofEpochMilli(anchorMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val diff = ChronoUnit.DAYS.between(anchorDate, today)
                diff >= 0 && diff % n == 0L
            }
            ScheduleType.CUSTOM -> false
        }
}
