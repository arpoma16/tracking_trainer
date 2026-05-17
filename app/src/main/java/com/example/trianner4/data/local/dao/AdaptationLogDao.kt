package com.example.trianner4.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.trianner4.data.local.entity.AdaptationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdaptationLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AdaptationLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<AdaptationLogEntity>): List<Long>

    @Query("SELECT * FROM adaptation_log WHERE sessionId = :sessionId")
    suspend fun getForSession(sessionId: Long): List<AdaptationLogEntity>

    /** Historial completo de adaptaciones causadas por una molestia. */
    @Query("""
        SELECT * FROM adaptation_log
        WHERE  discomfortId = :discomfortId
        ORDER  BY sessionId DESC
    """)
    fun observeForDiscomfort(discomfortId: Long): Flow<List<AdaptationLogEntity>>

    /** Historial de adaptaciones para la pantalla Bienestar (todas las molestias). */
    @Query("SELECT * FROM adaptation_log ORDER BY sessionId DESC")
    fun observeAll(): Flow<List<AdaptationLogEntity>>
}
