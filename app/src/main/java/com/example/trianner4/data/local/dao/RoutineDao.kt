package com.example.trianner4.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trianner4.data.local.entity.RoutineEntity
import com.example.trianner4.data.local.entity.RoutinePhaseExerciseEntity
import com.example.trianner4.data.local.entity.RoutineScheduleEntity
import com.example.trianner4.data.local.model.RoutinePhaseExerciseWithExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    // ── Rutinas ────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routine: RoutineEntity): Long

    @Update
    suspend fun update(routine: RoutineEntity)

    @Delete
    suspend fun delete(routine: RoutineEntity)

    @Query("SELECT * FROM routine WHERE id = :id")
    suspend fun getById(id: Long): RoutineEntity?

    @Query("SELECT * FROM routine WHERE isActive = 1 ORDER BY name ASC")
    fun observeActive(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routine ORDER BY name ASC")
    fun observeAll(): Flow<List<RoutineEntity>>

    // ── Horarios (RoutineSchedule) ─────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: RoutineScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: RoutineScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: RoutineScheduleEntity)

    @Query("SELECT * FROM routine_schedule WHERE routineId = :routineId")
    suspend fun getSchedulesForRoutine(routineId: Long): List<RoutineScheduleEntity>

    // ── Ejercicios por fase (RoutinePhaseExercise) ────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhaseExercise(rpe: RoutinePhaseExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhaseExercises(rpes: List<RoutinePhaseExerciseEntity>): List<Long>

    @Update
    suspend fun updatePhaseExercise(rpe: RoutinePhaseExerciseEntity)

    @Delete
    suspend fun deletePhaseExercise(rpe: RoutinePhaseExerciseEntity)

    @Query("DELETE FROM routine_phase_exercise WHERE routineId = :routineId")
    suspend fun clearPhaseExercises(routineId: Long)

    /** Stream reactivo para el editor de rutina (reordena drag & drop). */
    @Query("""
        SELECT * FROM routine_phase_exercise
        WHERE  routineId = :routineId
        ORDER  BY phase, orderIndex
    """)
    fun observePhaseExercises(routineId: Long): Flow<List<RoutinePhaseExerciseEntity>>

    /**
     * Ejercicios de una rutina con los datos completos del ejercicio.
     * Se usa para construir los SessionExerciseSnapshot al iniciar sesión,
     * congelando el nombre y el nivel de cadena vigentes en ese instante.
     */
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
            e.id           AS ex_id,
            e.name         AS ex_name,
            e.type         AS ex_type,
            e.trackingMode AS ex_trackingMode,
            e.description  AS ex_description,
            e.mediaRef     AS ex_mediaRef,
            e.primaryBodyZoneId AS ex_primaryBodyZoneId,
            e.chainId      AS ex_chainId
        FROM routine_phase_exercise rpe
        INNER JOIN exercise e ON e.id = rpe.exerciseId
        WHERE rpe.routineId = :routineId
        ORDER BY rpe.phase, rpe.orderIndex
    """)
    suspend fun getPhaseExercisesWithExercise(
        routineId: Long
    ): List<RoutinePhaseExerciseWithExercise>

    /**
     * Actualiza la variante mecánica activa tras una graduación.
     * Solo afecta al vínculo en la rutina; el historial de SetLog
     * conserva el nivel anterior (inmutable).
     */
    @Query("""
        UPDATE routine_phase_exercise
        SET    chainVariantId = :newVariantId
        WHERE  routineId = :routineId AND exerciseId = :exerciseId
    """)
    suspend fun updateChainVariant(routineId: Long, exerciseId: Long, newVariantId: Long)
}
