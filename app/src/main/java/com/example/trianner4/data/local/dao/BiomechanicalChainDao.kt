package com.example.trianner4.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.trianner4.data.local.entity.BiomechanicalChainEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BiomechanicalChainDao {
    @Query("SELECT * FROM biomechanical_chain ORDER BY name ASC")
    fun observeAll(): Flow<List<BiomechanicalChainEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chain: BiomechanicalChainEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chains: List<BiomechanicalChainEntity>)
}
