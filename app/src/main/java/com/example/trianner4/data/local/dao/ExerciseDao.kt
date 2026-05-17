package com.example.trianner4.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trianner4.data.local.ExerciseType
import com.example.trianner4.data.local.entity.ExerciseEntity
import com.example.trianner4.data.local.entity.ExerciseTagEntity
import com.example.trianner4.data.local.model.AffectedExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    // ── CRUD básico ────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>): List<Long>

    @Update
    suspend fun update(exercise: ExerciseEntity)

    @Delete
    suspend fun delete(exercise: ExerciseEntity)

    @Query("SELECT * FROM exercise WHERE id = :id")
    suspend fun getById(id: Long): ExerciseEntity?

    @Query("SELECT * FROM exercise ORDER BY name ASC")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercise WHERE type = :type ORDER BY name ASC")
    fun observeByType(type: ExerciseType): Flow<List<ExerciseEntity>>

    // ── Tags de ejercicio ──────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(exerciseTag: ExerciseTagEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(exerciseTags: List<ExerciseTagEntity>)

    @Delete
    suspend fun deleteTag(exerciseTag: ExerciseTagEntity)

    @Query("SELECT * FROM exercise_tag WHERE exerciseId = :exerciseId")
    suspend fun getTagsForExercise(exerciseId: Long): List<ExerciseTagEntity>

    // ── Consulta clave: ejercicios de una rutina afectados por molestias ──────
    //
    // Cruza los tags de cada ejercicio planificado en la rutina (:routineId)
    // con los tags de los Discomfort activos (isActive = 1).
    //
    // Una fila por par (RoutinePhaseExercise, Discomfort), lo que permite al
    // resolver de adaptación aplicar el factor de carga de cada molestia de
    // forma independiente y quedarse con el más restrictivo si hay varias.
    //
    // GROUP BY rpe.id, d.id colapsa duplicados cuando un ejercicio comparte
    // más de un tag con la misma molestia.
    // ── Ejercicios inyectables por adaptación ─────────────────────────────────

    /**
     * Ejercicios ASSISTIVE con tag GOAL="movilidad" que comparten al menos
     * un tag con alguna molestia activa. Se inyectan en la Fase PRE.
     * Excluye los que ya están en la rutina para esa fase.
     */
    @Query("""
        SELECT DISTINCT e.*
        FROM exercise e
        INNER JOIN exercise_tag et_goal ON et_goal.exerciseId = e.id
        INNER JOIN tag t_goal ON t_goal.id = et_goal.tagId
        WHERE e.type = 'ASSISTIVE'
          AND t_goal.category = 'GOAL'
          AND t_goal.name = 'movilidad'
          AND e.id IN (
              SELECT et2.exerciseId FROM exercise_tag et2
              INNER JOIN discomfort_tag dt ON dt.tagId = et2.tagId
              INNER JOIN discomfort d ON d.id = dt.discomfortId
              WHERE d.isActive = 1
          )
          AND e.id NOT IN (
              SELECT exerciseId FROM routine_phase_exercise
              WHERE routineId = :routineId AND phase = 'PRE'
          )
        LIMIT :maxInject
    """)
    suspend fun getInjectablePreExercises(
        routineId: Long,
        maxInject: Int = 3
    ): List<ExerciseEntity>

    /**
     * Ejercicios ASSISTIVE con tag GOAL en ('masaje','estiramiento','recuperacion')
     * que comparten al menos un tag con alguna molestia activa. Se inyectan en POST.
     */
    @Query("""
        SELECT DISTINCT e.*
        FROM exercise e
        INNER JOIN exercise_tag et_goal ON et_goal.exerciseId = e.id
        INNER JOIN tag t_goal ON t_goal.id = et_goal.tagId
        WHERE e.type = 'ASSISTIVE'
          AND t_goal.category = 'GOAL'
          AND t_goal.name IN ('masaje', 'estiramiento', 'recuperacion')
          AND e.id IN (
              SELECT et2.exerciseId FROM exercise_tag et2
              INNER JOIN discomfort_tag dt ON dt.tagId = et2.tagId
              INNER JOIN discomfort d ON d.id = dt.discomfortId
              WHERE d.isActive = 1
          )
          AND e.id NOT IN (
              SELECT exerciseId FROM routine_phase_exercise
              WHERE routineId = :routineId AND phase = 'POST'
          )
        LIMIT :maxInject
    """)
    suspend fun getInjectablePostExercises(
        routineId: Long,
        maxInject: Int = 3
    ): List<ExerciseEntity>

    // ── Consulta clave: ejercicios de una rutina afectados por molestias ──────

    @Query("""
        SELECT
            rpe.id,
            rpe.routineId,
            rpe.phase,
            rpe.exerciseId,
            rpe.orderIndex,
            rpe.targetSets,
            rpe.targetReps,
            rpe.targetRir,
            rpe.targetDurationSec,
            rpe.restSec,
            rpe.chainVariantId,
            e.id          AS ex_id,
            e.name        AS ex_name,
            e.type        AS ex_type,
            e.trackingMode AS ex_trackingMode,
            e.description  AS ex_description,
            e.mediaRef     AS ex_mediaRef,
            e.primaryBodyZoneId AS ex_primaryBodyZoneId,
            e.chainId      AS ex_chainId,
            d.id           AS discomfortId,
            d.label        AS discomfortLabel,
            d.severity     AS discomfortSeverity
        FROM routine_phase_exercise rpe
        INNER JOIN exercise           e  ON e.id  = rpe.exerciseId
        INNER JOIN exercise_tag       et ON et.exerciseId = e.id
        INNER JOIN discomfort_tag     dt ON dt.tagId      = et.tagId
        INNER JOIN discomfort         d  ON d.id  = dt.discomfortId
        WHERE rpe.routineId = :routineId
          AND d.isActive = 1
        GROUP BY rpe.id, d.id
        ORDER BY rpe.phase, rpe.orderIndex
    """)
    suspend fun getExercisesAffectedByActiveDiscomforts(routineId: Long): List<AffectedExercise>
}
