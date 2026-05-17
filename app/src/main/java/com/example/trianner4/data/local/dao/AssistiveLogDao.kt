package com.example.trianner4.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.trianner4.data.local.entity.AssistiveLogEntity

// AssistiveLog es inmutable tras cerrar la sesión: no hay métodos update ni delete.
@Dao
interface AssistiveLogDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(log: AssistiveLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(logs: List<AssistiveLogEntity>): List<Long>

    @Query("SELECT * FROM assistive_log WHERE snapshotId = :snapshotId")
    suspend fun getForSnapshot(snapshotId: Long): AssistiveLogEntity?

    /**
     * Ejercicios inyectados por una molestia específica.
     * Alimenta el historial de adaptaciones en la pantalla Bienestar.
     */
    @Query("""
        SELECT al.* FROM assistive_log al
        WHERE  al.injectedByDiscomfortId = :discomfortId
        ORDER  BY al.snapshotId DESC
    """)
    suspend fun getInjectedByDiscomfort(discomfortId: Long): List<AssistiveLogEntity>

    /**
     * Score de alivio promedio reportado para ejercicios inyectados
     * por una molestia, útil para evaluar efectividad del protocolo terapéutico.
     */
    @Query("""
        SELECT AVG(reliefScore) FROM assistive_log
        WHERE  injectedByDiscomfortId = :discomfortId
          AND  reliefScore IS NOT NULL
    """)
    suspend fun getAvgReliefScoreForDiscomfort(discomfortId: Long): Double?
}
