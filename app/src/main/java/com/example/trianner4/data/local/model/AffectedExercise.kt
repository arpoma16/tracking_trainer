package com.example.trianner4.data.local.model

import androidx.room.Embedded
import com.example.trianner4.data.local.entity.ExerciseEntity
import com.example.trianner4.data.local.entity.RoutinePhaseExerciseEntity

/**
 * Resultado de la consulta que cruza los ejercicios de una rutina
 * con los tags de los Discomfort activos del usuario.
 *
 * Por cada par (ejercicio, molestia) que comparte al menos un tag se
 * emite una fila. Si un mismo ejercicio es afectado por dos molestias
 * distintas aparecerán dos filas, lo que permite al resolver aplicar
 * el factor de carga correcto para cada una.
 */
data class AffectedExercise(
    @Embedded
    val routineConfig: RoutinePhaseExerciseEntity,

    @Embedded(prefix = "ex_")
    val exercise: ExerciseEntity,

    val discomfortId: Long,
    val discomfortLabel: String,
    val discomfortSeverity: Int
)
