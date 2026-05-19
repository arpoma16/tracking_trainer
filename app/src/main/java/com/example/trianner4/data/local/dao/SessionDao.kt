package com.example.trianner4.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.trianner4.data.local.SessionStatus
import com.example.trianner4.data.local.entity.SessionEntity
import com.example.trianner4.data.local.entity.SessionExerciseSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SessionDao {

    // ── Inserts / deletes internos (solo los @Transaction los llaman) ───────────

    @Insert
    protected abstract suspend fun insertSession(session: SessionEntity): Long

    @Insert
    protected abstract suspend fun insertSnapshots(
        snapshots: List<SessionExerciseSnapshotEntity>
    ): List<Long>

    @Query("DELETE FROM session_exercise_snapshot WHERE sessionId = :sessionId")
    protected abstract suspend fun deleteSnapshotsForSession(sessionId: Long)

    // ── Transacción clave: crear sesión + congelar snapshot del día ───────────
    //
    // El Repository construye la lista de snapshots con:
    //   - exerciseNameSnapshot  = nombre actual del ejercicio (defensa contra renombrados)
    //   - chainVariantLevelSnapshot = nivel mecánico vigente en este momento
    //   - sessionId = 0 (se sobreescribe aquí con el id generado)
    //
    // Toda la operación es atómica: si falla algún insert, Room hace rollback.
    @Transaction
    open suspend fun createSessionWithSnapshots(
        session: SessionEntity,
        snapshots: List<SessionExerciseSnapshotEntity>
    ): Long {
        val sessionId = insertSession(session)
        if (snapshots.isNotEmpty()) {
            insertSnapshots(snapshots.map { it.copy(sessionId = sessionId) })
        }
        return sessionId
    }

    // ── Reemplazar snapshots (solo si aún no hay logs — sesión sin iniciar) ─────
    //
    // Usado cuando la rutina cambia después de crear la sesión pero antes de
    // registrar cualquier serie. Operación atómica: borra los snapshots anteriores
    // e inserta los nuevos con el mismo sessionId.
    @Transaction
    open suspend fun replaceSnapshots(
        sessionId: Long,
        snapshots: List<SessionExerciseSnapshotEntity>
    ) {
        deleteSnapshotsForSession(sessionId)
        if (snapshots.isNotEmpty()) {
            insertSnapshots(snapshots.map { it.copy(sessionId = sessionId) })
        }
    }

    // ── Cierre de sesión (escribe agregados, marca como inmutable) ────────────
    //
    // Una vez ejecutado, los SetLog y AssistiveLog vinculados no deben
    // modificarse. La inmutabilidad se refuerza no exponiendo métodos
    // update/delete en SetLogDao y AssistiveLogDao.
    @Query("""
        UPDATE session
        SET    endedAt        = :endedAt,
               status         = :status,
               totalTonnage   = :totalTonnage,
               avgRir         = :avgRir,
               fatigueScore   = :fatigueScore,
               painScore      = :painScore,
               readinessScore = :readinessScore
        WHERE  id = :sessionId
    """)
    abstract suspend fun closeSession(
        sessionId: Long,
        endedAt: Long,
        status: SessionStatus,
        totalTonnage: Double,
        avgRir: Double,
        fatigueScore: Int,
        painScore: Int,
        readinessScore: Int
    )

    // ── Lecturas ───────────────────────────────────────────────────────────────

    @Query("SELECT * FROM session WHERE id = :id")
    abstract suspend fun getById(id: Long): SessionEntity?

    /** Sesión cuyo status es ACTIVE (máximo una en simultaneo). */
    @Query("SELECT * FROM session WHERE status = 'ACTIVE' LIMIT 1")
    abstract suspend fun getActiveSession(): SessionEntity?

    /** Sesión registrada en una fecha específica (epoch millis de medianoche). */
    @Query("SELECT * FROM session WHERE date = :dateEpoch LIMIT 1")
    abstract suspend fun getByDate(dateEpoch: Long): SessionEntity?

    /** Flow reactivo: emite la sesión de un día concreto cada vez que cambia. */
    @Query("SELECT * FROM session WHERE date = :dateEpoch LIMIT 1")
    abstract fun observeByDate(dateEpoch: Long): Flow<SessionEntity?>

    /** Flow reactivo: todas las sesiones de un día (puede haber varias rutinas). */
    @Query("SELECT * FROM session WHERE date = :dateEpoch")
    abstract fun observeAllByDate(dateEpoch: Long): Flow<List<SessionEntity>>

    /** Stream de todas las sesiones, descendente, para el Calendario. */
    @Query("SELECT * FROM session ORDER BY date DESC")
    abstract fun observeAll(): Flow<List<SessionEntity>>

    /** Sesiones dentro de un rango de fechas para la vista mensual. */
    @Query("""
        SELECT * FROM session
        WHERE  date BETWEEN :startEpoch AND :endEpoch
        ORDER  BY date ASC
    """)
    abstract fun observeInRange(startEpoch: Long, endEpoch: Long): Flow<List<SessionEntity>>

    /** N sesiones más recientes completadas (para el motor de progresión). */
    @Query("""
        SELECT * FROM session
        WHERE  status IN ('COMPLETED', 'ADAPTED', 'DELOAD')
        ORDER  BY date DESC
        LIMIT  :limit
    """)
    abstract suspend fun getRecentCompleted(limit: Int): List<SessionEntity>

    /** Número de sesiones completadas cuya fecha cae dentro de [startEpoch, endEpoch]. */
    @Query("""
        SELECT COUNT(*) FROM session
        WHERE  date BETWEEN :startEpoch AND :endEpoch
          AND  status IN ('COMPLETED', 'ADAPTED', 'DELOAD')
    """)
    abstract suspend fun countCompletedInRange(startEpoch: Long, endEpoch: Long): Int

    // ── Motor de progresión ────────────────────────────────────────────────────

    /**
     * Sesiones completadas (cualquier status final) que contienen un ejercicio
     * específico. Ordenadas de más reciente a más antigua.
     * Usada por [CheckGraduationEligibilityUseCase] para analizar el historial.
     */
    @Query("""
        SELECT DISTINCT s.* FROM session s
        INNER JOIN session_exercise_snapshot ses ON ses.sessionId = s.id
        WHERE  ses.exerciseId = :exerciseId
          AND  s.status IN ('COMPLETED', 'ADAPTED', 'DELOAD')
        ORDER  BY s.date DESC
        LIMIT  :limit
    """)
    abstract suspend fun getRecentCompletedSessionsWithExercise(
        exerciseId: Long,
        limit: Int
    ): List<SessionEntity>

    /**
     * Snapshot de un ejercicio concreto dentro de una sesión.
     * Devuelve el primero si el ejercicio aparece más de una vez (no debería ocurrir).
     */
    @Query("""
        SELECT * FROM session_exercise_snapshot
        WHERE  sessionId = :sessionId AND exerciseId = :exerciseId
        LIMIT  1
    """)
    abstract suspend fun getSnapshotForExerciseInSession(
        sessionId: Long,
        exerciseId: Long
    ): SessionExerciseSnapshotEntity?

    // ── Snapshots ─────────────────────────────────────────────────────────────

    @Query("""
        SELECT * FROM session_exercise_snapshot
        WHERE  sessionId = :sessionId
        ORDER  BY phase, orderIndex
    """)
    abstract suspend fun getSnapshotsForSession(
        sessionId: Long
    ): List<SessionExerciseSnapshotEntity>

    @Query("""
        SELECT * FROM session_exercise_snapshot
        WHERE  sessionId = :sessionId AND phase = :phase
        ORDER  BY orderIndex
    """)
    abstract suspend fun getSnapshotsForPhase(
        sessionId: Long,
        phase: String
    ): List<SessionExerciseSnapshotEntity>
}
