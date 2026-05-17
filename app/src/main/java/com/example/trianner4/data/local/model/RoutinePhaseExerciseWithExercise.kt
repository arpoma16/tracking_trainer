package com.example.trianner4.data.local.model

import androidx.room.Embedded
import com.example.trianner4.data.local.entity.ExerciseEntity
import com.example.trianner4.data.local.entity.RoutinePhaseExerciseEntity

/**
 * Combina la configuración de un ejercicio en una rutina (sets, reps,
 * RIR objetivo, fase, etc.) con los datos inmutables del ejercicio
 * (nombre, tipo, modo de tracking). Se usa al construir la vista previa
 * de sesión y al generar los SessionExerciseSnapshot.
 */
data class RoutinePhaseExerciseWithExercise(
    @Embedded
    val routinePhaseExercise: RoutinePhaseExerciseEntity,

    @Embedded(prefix = "ex_")
    val exercise: ExerciseEntity
)
