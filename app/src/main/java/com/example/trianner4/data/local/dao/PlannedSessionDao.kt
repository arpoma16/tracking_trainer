package com.example.trianner4.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trianner4.data.local.entity.PlannedSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannedSessionDao {

    /** INSERT OR IGNORE — safe to call idempotently for already-materialized dates. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(planned: PlannedSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(plans: List<PlannedSessionEntity>): List<Long>

    @Update
    suspend fun update(planned: PlannedSessionEntity)

    // ── Roll-forward ───────────────────────────────────────────────────────────

    /** PENDING sessions whose plannedDate is strictly before today (overdue), oldest first. */
    @Query("""
        SELECT * FROM planned_session
        WHERE  status = 'PENDING' AND plannedDate < :todayEpoch
        ORDER  BY plannedDate ASC
    """)
    suspend fun getOverduePending(todayEpoch: Long): List<PlannedSessionEntity>

    @Query("UPDATE planned_session SET status = 'ROLLED_FORWARD' WHERE id = :id")
    suspend fun markRolledForward(id: Long)

    // ── Completion ─────────────────────────────────────────────────────────────

    @Query("SELECT * FROM planned_session WHERE routineId = :routineId AND plannedDate = :dateEpoch LIMIT 1")
    suspend fun getByRoutineAndDate(routineId: Long, dateEpoch: Long): PlannedSessionEntity?

    @Query("UPDATE planned_session SET status = 'COMPLETED', linkedSessionId = :sessionId WHERE id = :id")
    suspend fun markCompleted(id: Long, sessionId: Long)

    // ── Streak calculation ─────────────────────────────────────────────────────

    /**
     * Returns the most recent planned sessions up to and including today,
     * excluding ROLLED_FORWARD entries (which are superseded by their replacements).
     * Used by [UpdateStreakUseCase] to compute the adherence streak.
     */
    @Query("""
        SELECT * FROM planned_session
        WHERE  plannedDate <= :todayEpoch
          AND  status != 'ROLLED_FORWARD'
        ORDER  BY plannedDate DESC
        LIMIT  :limit
    """)
    suspend fun getRecentForAdherenceStreak(todayEpoch: Long, limit: Int): List<PlannedSessionEntity>

    // ── Observation ────────────────────────────────────────────────────────────

    @Query("SELECT * FROM planned_session WHERE plannedDate = :dateEpoch ORDER BY routineId")
    fun observeForDate(dateEpoch: Long): Flow<List<PlannedSessionEntity>>
}
