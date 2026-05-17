package com.example.trianner4.ui.rutinas

import com.example.trianner4.data.local.entity.RoutineEntity
import com.example.trianner4.data.local.entity.RoutineScheduleEntity

data class RoutineWithSchedule(
    val routine: RoutineEntity,
    val schedules: List<RoutineScheduleEntity>
)

sealed interface RutinasUiState {
    data object Loading : RutinasUiState
    data class Ready(val routines: List<RoutineWithSchedule>) : RutinasUiState
    data class Error(val message: String) : RutinasUiState
}
