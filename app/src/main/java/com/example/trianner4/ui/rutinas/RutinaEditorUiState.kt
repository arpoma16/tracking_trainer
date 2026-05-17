package com.example.trianner4.ui.rutinas

import com.example.trianner4.data.local.Phase
import com.example.trianner4.data.local.entity.ExerciseEntity
import com.example.trianner4.data.local.entity.RoutineEntity
import com.example.trianner4.data.local.entity.RoutinePhaseExerciseEntity
import com.example.trianner4.data.local.model.RoutinePhaseExerciseWithExercise

data class RutinaEditorUiState(
    val name: String = "",
    val frequencyDays: Int = 1,
    val preExercises: List<RoutinePhaseExerciseWithExercise> = emptyList(),
    val coreExercises: List<RoutinePhaseExerciseWithExercise> = emptyList(),
    val postExercises: List<RoutinePhaseExerciseWithExercise> = emptyList(),
    val isSaving: Boolean = false,
    val showExercisePicker: Phase? = null,
    val searchQuery: String = ""
)
