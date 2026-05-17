package com.example.trianner4.domain.progression

/**
 * Resultado del análisis de elegibilidad para promover un ejercicio
 * a su siguiente variante mecánica en la cadena biomecánica.
 */
sealed class GraduationEligibilityResult {

    /** El ejercicio no pertenece a ninguna cadena, ya está en el nivel máximo,
     *  o no ha acumulado suficientes sesiones exitosas consecutivas. */
    data object NotEligible : GraduationEligibilityResult()

    /**
     * El usuario cumple el trigger. Contiene todo lo necesario para
     * renderizar el diálogo de criterio de graduación.
     *
     * @param exerciseId          Id del ejercicio actual.
     * @param exerciseName        Nombre del ejercicio actual (para el header del diálogo).
     * @param currentVariantId    Id de la ChainVariant actual.
     * @param currentLevel        Nivel mecánico actual (1..N).
     * @param nextVariantId       Id de la siguiente ChainVariant a desbloquear.
     * @param nextExerciseId      Id del Exercise de la siguiente variante.
     * @param nextExerciseName    Nombre del ejercicio de la siguiente variante.
     * @param nextLevel           Nivel de la siguiente variante.
     * @param criterionId         Id del GraduationCriterion; 0 si no existe (casos legacy).
     * @param consecutiveSuccessfulSessions Sesiones consecutivas exitosas detectadas.
     * @param requiredSessions    Umbral necesario (del ProgressionTrigger o default).
     * @param requireFullRom      Si el criterio exige confirmar ROM completo.
     * @param requireZeroPain     Si el criterio exige confirmar dolor 0/10.
     * @param suggestedInitialWeightKg Carga sugerida para el primer entrenamiento
     *                            con la nueva variante (60 % del último peso registrado).
     */
    data class Eligible(
        val exerciseId: Long,
        val exerciseName: String,
        val currentVariantId: Long,
        val currentLevel: Int,
        val nextVariantId: Long,
        val nextExerciseId: Long,
        val nextExerciseName: String,
        val nextLevel: Int,
        val criterionId: Long,
        val consecutiveSuccessfulSessions: Int,
        val requiredSessions: Int,
        val requireFullRom: Boolean,
        val requireZeroPain: Boolean,
        val suggestedInitialWeightKg: Double?
    ) : GraduationEligibilityResult()
}
