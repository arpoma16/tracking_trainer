package com.example.trianner4.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.trianner4.data.local.entity.ProgressionTriggerEntity

@Dao
interface ProgressionTriggerDao {

    /** Config específica del ejercicio; null si usa los defaults globales. */
    @Query("SELECT * FROM progression_trigger WHERE exerciseId = :exerciseId LIMIT 1")
    suspend fun getForExercise(exerciseId: Long): ProgressionTriggerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(trigger: ProgressionTriggerEntity): Long
}
