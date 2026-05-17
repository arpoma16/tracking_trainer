package com.example.trianner4.domain.progression

import com.example.trianner4.data.local.dao.ChainVariantDao
import com.example.trianner4.data.local.dao.ExerciseDao
import com.example.trianner4.data.local.dao.ProgressionTriggerDao
import com.example.trianner4.data.local.dao.SessionDao
import com.example.trianner4.data.local.dao.SetLogDao
import javax.inject.Inject

/**
 * Analiza las últimas sesiones de un ejercicio STRENGTH y determina si el usuario
 * cumple el trigger de graduación a la siguiente variante mecánica.
 *
 * Reglas (según ARQUITECTURA.md § 4.B):
 *  - N sesiones CONSECUTIVAS (de la más reciente hacia atrás) donde:
 *      · Todas las series completadas tienen RIR ≥ minRirRequired.
 *      · La sesión no registró painScore ≥ 4.
 *  - N y minRirRequired provienen del ProgressionTrigger del ejercicio;
 *    si no existe, se usan los defaults (5 sesiones / RIR 2).
 *
 * Efecto lateral: cuando la elegibilidad se confirma, marca
 * ChainVariant.eligibleForPromotion = true para que la UI pueda
 * mostrar el indicador (punto verde) sin re-evaluar cada vez.
 */
class CheckGraduationEligibilityUseCase @Inject constructor(
    private val sessionDao: SessionDao,
    private val setLogDao: SetLogDao,
    private val chainVariantDao: ChainVariantDao,
    private val progressionTriggerDao: ProgressionTriggerDao,
    private val exerciseDao: ExerciseDao
) {

    suspend operator fun invoke(
        exerciseId: Long,
        routineId: Long
    ): GraduationEligibilityResult {

        // ── 1. Configuración del trigger ───────────────────────────────────────
        val trigger = progressionTriggerDao.getForExercise(exerciseId)
        val sessionsNeeded = trigger?.successfulSessionsNeeded ?: DEFAULT_SESSIONS_NEEDED
        val minRir = trigger?.minRirRequired ?: DEFAULT_MIN_RIR

        // ── 2. Variante actual en la rutina ────────────────────────────────────
        val currentVariant =
            chainVariantDao.getCurrentVariantForRoutineExercise(routineId, exerciseId)
                ?: return GraduationEligibilityResult.NotEligible

        // ── 3. Existe una variante superior ────────────────────────────────────
        val nextVariant =
            chainVariantDao.getVariantAtLevel(currentVariant.chainId, currentVariant.level + 1)
                ?: return GraduationEligibilityResult.NotEligible

        // ── 4. Sesiones recientes que contienen este ejercicio ─────────────────
        // Se obtiene el doble del umbral como buffer para absorber sesiones fallidas
        // entre las exitosas, sin sobrecargar la consulta.
        val recentSessions = sessionDao.getRecentCompletedSessionsWithExercise(
            exerciseId = exerciseId,
            limit = sessionsNeeded * 2
        )

        // ── 5. Contar sesiones CONSECUTIVAS exitosas (de reciente a antiguo) ───
        var consecutive = 0
        for (session in recentSessions) {
            val snapshot =
                sessionDao.getSnapshotForExerciseInSession(session.id, exerciseId) ?: break

            val completedSets = setLogDao.getForSnapshot(snapshot.id).filter { it.isCompleted }
            if (completedSets.isEmpty()) break

            val allSetsPassRir = completedSets.all { it.rir >= minRir }
            val sessionPainOk = (session.painScore ?: 0) < PAIN_THRESHOLD

            if (allSetsPassRir && sessionPainOk) consecutive++ else break
        }

        if (consecutive < sessionsNeeded) return GraduationEligibilityResult.NotEligible

        // ── 6. Construir resultado Eligible ────────────────────────────────────
        val currentExercise =
            exerciseDao.getById(exerciseId) ?: return GraduationEligibilityResult.NotEligible
        val nextExercise =
            exerciseDao.getById(nextVariant.exerciseId) ?: return GraduationEligibilityResult.NotEligible
        val criterion = chainVariantDao.getCriterionForVariant(currentVariant.id)

        // Carga sugerida: 60 % del último peso registrado para este ejercicio
        val suggestedWeight = setLogDao
            .getRecentCompletedSetsForExercise(exerciseId, limit = 1)
            .firstOrNull()
            ?.weightKg
            ?.let { it * INITIAL_LOAD_FACTOR }

        // Efecto lateral: marcar la variante como elegible para que la UI
        // pueda mostrar el indicador sin re-ejecutar el análisis completo.
        chainVariantDao.setEligible(currentVariant.id, true)

        return GraduationEligibilityResult.Eligible(
            exerciseId = exerciseId,
            exerciseName = currentExercise.name,
            currentVariantId = currentVariant.id,
            currentLevel = currentVariant.level,
            nextVariantId = nextVariant.id,
            nextExerciseId = nextVariant.exerciseId,
            nextExerciseName = nextExercise.name,
            nextLevel = nextVariant.level,
            criterionId = criterion?.id ?: 0L,
            consecutiveSuccessfulSessions = consecutive,
            requiredSessions = sessionsNeeded,
            requireFullRom = criterion?.requireFullRom ?: true,
            requireZeroPain = criterion?.requireZeroPain ?: true,
            suggestedInitialWeightKg = suggestedWeight
        )
    }

    companion object {
        private const val DEFAULT_SESSIONS_NEEDED = 5
        private const val DEFAULT_MIN_RIR = 2
        private const val PAIN_THRESHOLD = 4
        private const val INITIAL_LOAD_FACTOR = 0.60
    }
}
