package com.example.trianner4.ui.calendario

import com.example.trianner4.data.local.SessionStatus
import com.example.trianner4.data.local.entity.AdaptationLogEntity
import com.example.trianner4.data.local.entity.SessionEntity
import com.example.trianner4.data.local.entity.SessionExerciseSnapshotEntity
import java.time.LocalDate
import java.time.YearMonth

data class DayEntry(
    val date: LocalDate,
    val status: SessionStatus?,
    val isAdapted: Boolean = false,
    val isDeload: Boolean = false,
    val isFrozen: Boolean = false
)

data class MonthlySummary(
    val totalSessions: Int,
    val completedSessions: Int,
    val adaptedSessions: Int,
    val adherencePercent: Int
)

data class DayDetail(
    val session: SessionEntity,
    val snapshots: List<SessionExerciseSnapshotEntity>,
    val adaptations: List<AdaptationLogEntity>
)

sealed interface CalendarioUiState {
    data object Loading : CalendarioUiState

    data class Ready(
        val currentMonth: YearMonth,
        val days: List<DayEntry>,
        val summary: MonthlySummary,
        val selectedDayDetail: DayDetail? = null
    ) : CalendarioUiState

    data class Error(val message: String) : CalendarioUiState
}
