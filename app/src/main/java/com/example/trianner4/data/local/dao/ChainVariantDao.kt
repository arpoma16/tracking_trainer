package com.example.trianner4.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.trianner4.data.local.entity.ChainVariantEntity
import com.example.trianner4.data.local.entity.GraduationCriterionEntity

@Dao
abstract class ChainVariantDao {

    // ── ChainVariant ───────────────────────────────────────────────────────────

    /**
     * Variante activa de un ejercicio dentro de una rutina.
     * Navega por RoutinePhaseExercise.chainVariantId para encontrar la variante vigente.
     */
    @Query("""
        SELECT cv.* FROM chain_variant cv
        INNER JOIN routine_phase_exercise rpe ON rpe.chainVariantId = cv.id
        WHERE rpe.routineId = :routineId AND rpe.exerciseId = :exerciseId
        LIMIT 1
    """)
    abstract suspend fun getCurrentVariantForRoutineExercise(
        routineId: Long,
        exerciseId: Long
    ): ChainVariantEntity?

    /**
     * Variante en un nivel específico dentro de una cadena biomecánica.
     * Usada para obtener la siguiente variante (level = actual + 1).
     */
    @Query("""
        SELECT * FROM chain_variant
        WHERE chainId = :chainId AND level = :level
        LIMIT 1
    """)
    abstract suspend fun getVariantAtLevel(chainId: Long, level: Int): ChainVariantEntity?

    @Query("UPDATE chain_variant SET eligibleForPromotion = :eligible WHERE id = :variantId")
    abstract suspend fun setEligible(variantId: Long, eligible: Boolean)

    // ── GraduationCriterion ────────────────────────────────────────────────────

    @Query("SELECT * FROM graduation_criterion WHERE chainVariantId = :variantId LIMIT 1")
    abstract suspend fun getCriterionForVariant(variantId: Long): GraduationCriterionEntity?

    @Query("UPDATE graduation_criterion SET manualConfirmed = 1 WHERE id = :criterionId")
    abstract suspend fun confirmCriterion(criterionId: Long)

    // ── RoutinePhaseExercise (actualización atómica en graduación) ─────────────

    /**
     * Actualiza tanto chainVariantId como exerciseId en la rutina porque cada
     * variante mecánica apunta a un Exercise distinto.
     */
    @Query("""
        UPDATE routine_phase_exercise
        SET    chainVariantId = :newVariantId,
               exerciseId     = :newExerciseId
        WHERE  routineId  = :routineId
          AND  exerciseId = :currentExerciseId
    """)
    abstract suspend fun updatePhaseExerciseForPromotion(
        routineId: Long,
        currentExerciseId: Long,
        newVariantId: Long,
        newExerciseId: Long
    )

    // ── Transacción de graduación ──────────────────────────────────────────────

    /**
     * Ejecuta los 3 pasos de la graduación de forma atómica:
     *  1. Apunta la rutina a la nueva variante y su ejercicio.
     *  2. Registra la confirmación manual del criterio.
     *  3. Limpia el flag de elegibilidad de la variante previa.
     */
    @Transaction
    open suspend fun executeGraduation(
        routineId: Long,
        currentExerciseId: Long,
        currentVariantId: Long,
        newVariantId: Long,
        newExerciseId: Long,
        criterionId: Long
    ) {
        updatePhaseExerciseForPromotion(routineId, currentExerciseId, newVariantId, newExerciseId)
        if (criterionId > 0L) confirmCriterion(criterionId)
        setEligible(currentVariantId, false)
    }
}
