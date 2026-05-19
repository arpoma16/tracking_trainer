package com.example.trianner4.ui.calendario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trianner4.data.local.SessionStatus
import com.example.trianner4.data.local.dao.AdaptationLogDao
import com.example.trianner4.data.local.dao.SessionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class CalendarioViewModel @Inject constructor(
    private val sessionDao: SessionDao,
    private val adaptationLogDao: AdaptationLogDao
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    private val _selectedDayDetail = MutableStateFlow<DayDetail?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CalendarioUiState> = _currentMonth
        .flatMapLatest { month -> observeMonth(month) }
        .catch { e -> emit(CalendarioUiState.Error(e.message ?: "Error desconocido")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CalendarioUiState.Loading
        )

    fun navigateMonth(offset: Int) {
        _selectedDayDetail.value = null
        _currentMonth.update { it.plusMonths(offset.toLong()) }
    }

    fun selectDay(date: LocalDate) {
        viewModelScope.launch {
            val zone = ZoneId.systemDefault()
            val dateEpoch = date.atStartOfDay(zone).toInstant().toEpochMilli()
            val session = sessionDao.getByDate(dateEpoch) ?: return@launch
            val snapshots = sessionDao.getSnapshotsForSession(session.id)
            val adaptations = adaptationLogDao.getForSession(session.id)
            _selectedDayDetail.value = DayDetail(session, snapshots, adaptations)
        }
    }

    fun clearSelection() {
        _selectedDayDetail.value = null
    }

    private fun observeMonth(month: YearMonth) = combine(
        sessionDao.observeInRange(monthStartEpoch(month), monthEndEpoch(month)),
        _selectedDayDetail
    ) { sessions, detail ->
        val zone = ZoneId.systemDefault()
        val sessionByEpoch = sessions.associateBy { it.date }

        val days = (1..month.lengthOfMonth()).map { day ->
            val date = month.atDay(day)
            val epoch = date.atStartOfDay(zone).toInstant().toEpochMilli()
            val session = sessionByEpoch[epoch]
            DayEntry(
                date = date,
                status = session?.status,
                isAdapted = session?.isAdapted ?: false,
                isDeload = session?.isDeload ?: false
            )
        }

        val completed = sessions.count {
            it.status in setOf(SessionStatus.COMPLETED, SessionStatus.ADAPTED, SessionStatus.DELOAD)
        }

        CalendarioUiState.Ready(
            currentMonth = month,
            days = days,
            summary = MonthlySummary(
                totalSessions = sessions.size,
                completedSessions = completed,
                adaptedSessions = sessions.count { it.isAdapted },
                adherencePercent = if (sessions.isEmpty()) 0
                                   else (completed * 100 / sessions.size)
            ),
            selectedDayDetail = detail
        ) as CalendarioUiState
    }

    private fun monthStartEpoch(month: YearMonth): Long =
        month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun monthEndEpoch(month: YearMonth): Long =
        month.atEndOfMonth()
            .atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
}
