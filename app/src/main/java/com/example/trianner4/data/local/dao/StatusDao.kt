package com.example.trianner4.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.trianner4.data.local.entity.DeloadCycleEntity
import com.example.trianner4.data.local.entity.FreezePeriodEntity

@Dao
interface StatusDao {

    @Query("""
        SELECT * FROM deload_cycle
        WHERE startDate <= :todayEpoch AND endDate >= :todayEpoch
        LIMIT 1
    """)
    suspend fun getActiveDeload(todayEpoch: Long): DeloadCycleEntity?

    @Query("""
        SELECT * FROM freeze_period
        WHERE startDate <= :todayEpoch AND (endDate IS NULL OR endDate >= :todayEpoch)
        LIMIT 1
    """)
    suspend fun getActiveFreeze(todayEpoch: Long): FreezePeriodEntity?
}
