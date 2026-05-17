package com.example.trianner4.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.trianner4.data.local.entity.BodyZoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyZoneDao {
    @Query("SELECT * FROM body_zone ORDER BY level, name")
    fun getAll(): Flow<List<BodyZoneEntity>>

    @Query("SELECT * FROM body_zone WHERE parentId IS NULL ORDER BY name")
    fun getRootZones(): Flow<List<BodyZoneEntity>>

    @Query("SELECT * FROM body_zone WHERE parentId = :parentId ORDER BY name")
    fun getZonesByParent(parentId: Long): Flow<List<BodyZoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(zone: BodyZoneEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(zones: List<BodyZoneEntity>)
}
