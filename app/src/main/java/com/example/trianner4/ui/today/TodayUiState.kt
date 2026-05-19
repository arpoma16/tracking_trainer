package com.example.trianner4.ui.today

import com.example.trianner4.data.local.ExerciseType
import com.example.trianner4.data.local.TrackingMode

enum class DayStatus { TRAINING, REST, DELOAD, FROZEN, ADAPTED }

data class StreakData(
    val weeklyConsistency: Int,
    val routineAdherence: Int
)

data class ExerciseItem(
    val id: Long,
    val name: String,
    val type: ExerciseType,
    val trackingMode: TrackingMode,
    val isInjected: Boolean = false,
    val effectiveLoadFactor: Double? = null,
    val targetSets: Int? = null,
    val targetReps: Int? = null,
    val targetDurationSec: Int? = null,
    val discomfortLabel: String? = null
)

data class AdaptedPlan(
    val preExercises: List<ExerciseItem>,
    val coreExercises: List<ExerciseItem>,
    val postExercises: List<ExerciseItem>,
    val isAdapted: Boolean,
    val discomfortLabels: List<String>
)

data class TodayRoutineItem(
    val routineId: Long,
    val routineName: String,
    val plan: AdaptedPlan,
    val isDone: Boolean = false,
)

sealed interface TodayUiState {
    data object Loading : TodayUiState

    data class NoRoutineToday(
        val dayStatus: DayStatus,
        val streak: StreakData?
    ) : TodayUiState

    data class Ready(
        val routines: List<TodayRoutineItem>,
        val dayStatus: DayStatus,
        val streak: StreakData?,
    ) : TodayUiState
}
