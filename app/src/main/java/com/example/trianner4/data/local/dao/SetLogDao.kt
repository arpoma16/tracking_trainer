package com.example.trianner4.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.trianner4.data.local.entity.SetLogEntity

// SetLog es inmutable tras cerrar la sesión: no hay métodos update ni delete.
@Dao
interface SetLogDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(setLog: SetLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(setLogs: List<SetLogEntity>): List<Long>

    @Query("SELECT * FROM set_log WHERE snapshotId = :snapshotId ORDER BY setIndex")
    suspend fun getForSnapshot(snapshotId: Long): List<SetLogEntity>

    /** Todas las series de un ejercicio específico a través del historial de sesiones. */
    @Query("""
        SELECT sl.* FROM set_log sl
        INNER JOIN session_exercise_snapshot ses ON ses.id = sl.snapshotId
        WHERE ses.exerciseId = :exerciseId
        ORDER BY ses.sessionId DESC, sl.setIndex
    """)
    suspend fun getHistoryForExercise(exerciseId: Long): List<SetLogEntity>

    /**
     * Últimas N series completadas de un ejercicio.
     * Útil para el motor de progresión al evaluar si se cumple el
     * criterio de graduación o el trigger de sobrecarga.
     */
    @Query("""
        SELECT sl.* FROM set_log sl
        INNER JOIN session_exercise_snapshot ses ON ses.id = sl.snapshotId
        WHERE ses.exerciseId = :exerciseId
          AND sl.isCompleted = 1
        ORDER BY ses.sessionId DESC, sl.setIndex DESC
        LIMIT :limit
    """)
    suspend fun getRecentCompletedSetsForExercise(exerciseId: Long, limit: Int): List<SetLogEntity>

    /** Marca un set como PR. Se puede llamar al cerrar la sesión, antes de la inmutabilidad efectiva. */
    @Query("UPDATE set_log SET isPr = 1 WHERE id = :setLogId")
    suspend fun markAsPr(setLogId: Long)
}
