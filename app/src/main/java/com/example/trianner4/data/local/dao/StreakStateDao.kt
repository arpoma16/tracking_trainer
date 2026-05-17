package com.example.trianner4.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.trianner4.data.local.entity.StreakStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakStateDao {

    // Upsert: inserta si id=1 no existe, actualiza si ya existe.
    // Garantiza que el singleton nunca se duplique.
    @Upsert
    suspend fun upsert(state: StreakStateEntity)

    @Query("SELECT * FROM streak_state WHERE id = 1")
    suspend fun get(): StreakStateEntity?

    @Query("SELECT * FROM streak_state WHERE id = 1")
    fun observe(): Flow<StreakStateEntity?>

    @Query("""
        UPDATE streak_state
        SET    weeklyConsistencyCount       = :weeklyCount,
               weeklyConsistencyAnchorWeek  = :anchorWeek,
               routineAdherenceCount        = :adherenceCount,
               routineAdherenceAnchorDate   = :adherenceAnchor,
               lastUpdated                  = :updatedAt
        WHERE  id = 1
    """)
    suspend fun updateCounts(
        weeklyCount: Int,
        anchorWeek: Int,
        adherenceCount: Int,
        adherenceAnchor: Long,
        updatedAt: Long
    )
}
