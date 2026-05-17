package com.example.trianner4.domain.progression

import com.example.trianner4.data.local.dao.ChainVariantDao
import javax.inject.Inject

/**
 * Ejecuta la promoción de un ejercicio a su siguiente variante mecánica.
 *
 * Precondición: el usuario ha completado el checklist manual del diálogo
 * de criterio de graduación (manualChecksConfirmed = true).
 *
 * La operación es atómica (Room @Transaction en ChainVariantDao):
 *  1. Actualiza RoutinePhaseExercise → apunta a la nueva variante y su ejercicio.
 *  2. Registra GraduationCriterion.manualConfirmed = true en la variante anterior.
 *  3. Limpia ChainVariant.eligibleForPromotion en la variante anterior.
 */
class ConfirmGraduationUseCase @Inject constructor(
    private val chainVariantDao: ChainVariantDao
) {

    /**
     * @param eligible   Resultado devuelto por [CheckGraduationEligibilityUseCase].
     * @param routineId  Rutina en la que se aplica la promoción.
     * @param manualChecksConfirmed Todos los checks manuales del diálogo marcados.
     */
    suspend operator fun invoke(
        eligible: GraduationEligibilityResult.Eligible,
        routineId: Long,
        manualChecksConfirmed: Boolean
    ): Result<Unit> {
        if (!manualChecksConfirmed) {
            return Result.failure(
                IllegalStateException("El checklist manual debe estar completo antes de confirmar la graduación.")
            )
        }

        return runCatching {
            chainVariantDao.executeGraduation(
                routineId = routineId,
                currentExerciseId = eligible.exerciseId,
                currentVariantId = eligible.currentVariantId,
                newVariantId = eligible.nextVariantId,
                newExerciseId = eligible.nextExerciseId,
                criterionId = eligible.criterionId
            )
        }
    }
}
