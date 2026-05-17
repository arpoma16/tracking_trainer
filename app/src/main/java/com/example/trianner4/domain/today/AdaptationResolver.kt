package com.example.trianner4.domain.today

import com.example.trianner4.data.local.Phase
import com.example.trianner4.data.local.entity.DiscomfortEntity
import com.example.trianner4.data.local.entity.ExerciseEntity
import com.example.trianner4.data.local.model.AffectedExercise
import com.example.trianner4.data.local.model.RoutinePhaseExerciseWithExercise
import com.example.trianner4.ui.today.AdaptedPlan
import com.example.trianner4.ui.today.ExerciseItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdaptationResolver @Inject constructor() {

    /**
     * Aplica las adaptaciones por molestias sobre el plan de la rutina.
     *
     * - Los ejercicios del NÚCLEO afectados reciben un loadFactor < 1.0.
     * - Los ejercicios inyectables se añaden al final de PRE y POST
     *   sin duplicar ids ya presentes en cada fase.
     * - Si hay varias molestias afectando al mismo ejercicio, se aplica
     *   el factor más restrictivo (mínimo).
     */
    fun resolve(
        routineExercises: List<RoutinePhaseExerciseWithExercise>,
        activeDiscomforts: List<DiscomfortEntity>,
        affectedExercises: List<AffectedExercise>,
        injectablePre: List<ExerciseEntity>,
        injectablePost: List<ExerciseEntity>
    ): AdaptedPlan {
        if (activeDiscomforts.isEmpty()) {
            return buildUnchangedPlan(routineExercises)
        }

        // Máximo factor de reducción por rpe.id (el más restrictivo si hay varias molestias)
        val loadFactorByRpeId: Map<Long, Double> = affectedExercises
            .groupBy { it.routineConfig.id }
            .mapValues { (_, entries) ->
                entries.minOf { severityToLoadFactor(it.discomfortSeverity) }
            }

        val pre = mutableListOf<ExerciseItem>()
        val core = mutableListOf<ExerciseItem>()
        val post = mutableListOf<ExerciseItem>()

        for (rpe in routineExercises) {
            val loadFactor = loadFactorByRpeId[rpe.routinePhaseExercise.id]
            val item = ExerciseItem(
                id = rpe.exercise.id,
                name = rpe.exercise.name,
                type = rpe.exercise.type,
                trackingMode = rpe.exercise.trackingMode,
                effectiveLoadFactor = loadFactor,
                targetSets = rpe.routinePhaseExercise.targetSets,
                targetReps = rpe.routinePhaseExercise.targetReps,
                targetDurationSec = rpe.routinePhaseExercise.targetDurationSec
            )
            when (rpe.routinePhaseExercise.phase) {
                Phase.PRE -> pre.add(item)
                Phase.CORE -> core.add(item)
                Phase.POST -> post.add(item)
            }
        }

        val firstLabel = activeDiscomforts.firstOrNull()?.label

        // Inyectar en PRE sin duplicar ids
        val existingPreIds = pre.map { it.id }.toSet()
        for (ex in injectablePre) {
            if (ex.id !in existingPreIds) {
                pre.add(ex.toInjectedItem(firstLabel))
            }
        }

        // Inyectar en POST sin duplicar ids
        val existingPostIds = post.map { it.id }.toSet()
        for (ex in injectablePost) {
            if (ex.id !in existingPostIds) {
                post.add(ex.toInjectedItem(firstLabel))
            }
        }

        val isAdapted = loadFactorByRpeId.isNotEmpty()
                || injectablePre.isNotEmpty()
                || injectablePost.isNotEmpty()

        return AdaptedPlan(
            preExercises = pre,
            coreExercises = core,
            postExercises = post,
            isAdapted = isAdapted,
            discomfortLabels = activeDiscomforts.map { it.label }
        )
    }

    private fun buildUnchangedPlan(
        exercises: List<RoutinePhaseExerciseWithExercise>
    ): AdaptedPlan {
        val pre = mutableListOf<ExerciseItem>()
        val core = mutableListOf<ExerciseItem>()
        val post = mutableListOf<ExerciseItem>()
        for (rpe in exercises) {
            val item = ExerciseItem(
                id = rpe.exercise.id,
                name = rpe.exercise.name,
                type = rpe.exercise.type,
                trackingMode = rpe.exercise.trackingMode,
                targetSets = rpe.routinePhaseExercise.targetSets,
                targetReps = rpe.routinePhaseExercise.targetReps,
                targetDurationSec = rpe.routinePhaseExercise.targetDurationSec
            )
            when (rpe.routinePhaseExercise.phase) {
                Phase.PRE -> pre.add(item)
                Phase.CORE -> core.add(item)
                Phase.POST -> post.add(item)
            }
        }
        return AdaptedPlan(
            preExercises = pre,
            coreExercises = core,
            postExercises = post,
            isAdapted = false,
            discomfortLabels = emptyList()
        )
    }

    // severity 1-2 → 0.9  |  3 → 0.7  |  4 → 0.6  |  5 → 0.5
    private fun severityToLoadFactor(severity: Int): Double = when {
        severity <= 2 -> 0.9
        severity == 3 -> 0.7
        severity == 4 -> 0.6
        else -> 0.5
    }

    private fun ExerciseEntity.toInjectedItem(discomfortLabel: String?) = ExerciseItem(
        id = id,
        name = name,
        type = type,
        trackingMode = trackingMode,
        isInjected = true,
        discomfortLabel = discomfortLabel
    )
}
