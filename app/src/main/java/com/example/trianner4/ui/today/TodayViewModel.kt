package com.example.trianner4.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trianner4.data.local.SessionStatus
import com.example.trianner4.data.local.dao.SessionDao
import com.example.trianner4.domain.streak.MaterializePlannedSessionsUseCase
import com.example.trianner4.domain.streak.UpdateStreakUseCase
import com.example.trianner4.domain.today.TodayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    repository: TodayRepository,
    sessionDao: SessionDao,
    private val materializePlannedSessions: MaterializePlannedSessionsUseCase,
    private val updateStreak: UpdateStreakUseCase
) : ViewModel() {

    private val todayEpoch: Long = LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    val uiState: StateFlow<TodayUiState> = combine(
        repository.observeTodayState(),
        sessionDao.observeAllByDate(todayEpoch)
    ) { state, todaySessions ->
        if (state is TodayUiState.Ready) {
            val completedRoutineIds = todaySessions
                .filter { it.status in setOf(
                    SessionStatus.COMPLETED,
                    SessionStatus.ADAPTED,
                    SessionStatus.DELOAD,
                )}
                .map { it.routineId }
                .toSet()
            state.copy(
                routines = state.routines.map { r ->
                    r.copy(isDone = r.routineId in completedRoutineIds)
                }
            )
        } else state
    }
        .catch { emit(TodayUiState.NoRoutineToday(DayStatus.REST, null)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodayUiState.Loading
        )

    init {
        // On every app open: materialize upcoming sessions, roll forward any overdue
        // PENDING entries, and refresh streak counts.
        viewModelScope.launch {
            materializePlannedSessions()
            updateStreak()
        }
    }
}
