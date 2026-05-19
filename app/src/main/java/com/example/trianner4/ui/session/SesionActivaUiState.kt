package com.example.trianner4.ui.session

import com.example.trianner4.data.local.ExerciseType
import com.example.trianner4.data.local.Phase
import com.example.trianner4.data.local.TrackingMode
import com.example.trianner4.data.local.entity.AdaptationLogEntity
import com.example.trianner4.data.local.entity.SessionExerciseSnapshotEntity

/** Estado de una serie STRENGTH durante la sesión (antes de persistirse). */
data class PendingSet(
    val setIndex: Int,
    val targetReps: Int?,
    val weightKg: Double? = null,
    val bandTension: String? = null,
    val reps: Int = 0,
    val rir: Int = 2,
    val isCompleted: Boolean = false
)

/** Estado del temporizador de ejercicio asistencial. */
data class AssistiveTimerState(
    val targetDurationSec: Int?,
    val targetReps: Int?,
    val elapsedSec: Int = 0,
    val isRunning: Boolean = false,
    val isCompleted: Boolean = false
)

/** Temporizador de descanso inter-series. */
data class RestTimerState(
    val totalSec: Int,
    val remainingSec: Int,
    val isRunning: Boolean = true
)

/** Métricas agregadas al cerrar la sesión. */
data class SessionSummary(
    val sessionId: Long,
    val totalTonnageKg: Double,
    val durationMs: Long,
    val setsCompleted: Int,
    val setsSkipped: Int,
    val avgRir: Double,
    val adaptations: List<AdaptationLogEntity>
)

sealed interface SesionActivaUiState {

    /** Sin sesión iniciada o estado inicial. */
    data object Idle : SesionActivaUiState

    /** Cargando datos de la sesión desde Room. */
    data object Loading : SesionActivaUiState

    /** Ejercicio activo dentro de una de las 3 fases. */
    data class PhaseActive(
        val sessionId: Long,
        val phase: Phase,
        val exerciseIndex: Int,
        val totalExercisesInPhase: Int,
        val snapshot: SessionExerciseSnapshotEntity,
        val exerciseName: String,
        val exerciseType: ExerciseType,
        val trackingMode: TrackingMode,
        /** Series para ejercicios STRENGTH; vacío si es ASSISTIVE. */
        val pendingSets: List<PendingSet>,
        /** Estado del timer para ejercicios ASSISTIVE; null si es STRENGTH. */
        val assistiveTimer: AssistiveTimerState?,
        /** Temporizador de descanso entre series; null cuando no está corriendo. */
        val restTimer: RestTimerState?,
        /** Progreso global 0f–1f sobre el total de ejercicios de la sesión. */
        val overallProgress: Float,
        val isAdaptedExercise: Boolean = false,
        /** RIR objetivo para mostrar como referencia en ejercicios STRENGTH. */
        val targetRir: Int? = null,
        /** Descanso en segundos configurado para este ejercicio. */
        val restSec: Int = 90,
        /** True cuando es el último ejercicio de la última fase con contenido. */
        val isLastExerciseOverall: Boolean = false,
    ) : SesionActivaUiState

    /** Todos los ejercicios completados; pendiente de rellenar encuesta post-sesión. */
    data object SurveyPending : SesionActivaUiState

    /** Resumen final mostrado tras cerrar la sesión. */
    data class Summary(val data: SessionSummary) : SesionActivaUiState

    data class Error(val message: String) : SesionActivaUiState
}
