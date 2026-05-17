package com.example.trianner4.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trianner4.domain.streak.MaterializePlannedSessionsUseCase
import com.example.trianner4.domain.streak.UpdateStreakUseCase
import com.example.trianner4.domain.today.TodayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    repository: TodayRepository,
    private val materializePlannedSessions: MaterializePlannedSessionsUseCase,
    private val updateStreak: UpdateStreakUseCase
) : ViewModel() {

    val uiState: StateFlow<TodayUiState> = repository
        .observeTodayState()
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
