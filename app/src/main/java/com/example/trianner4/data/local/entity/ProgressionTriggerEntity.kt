package com.example.trianner4.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.trianner4.data.local.ProgressionMode

@Entity(
    tableName = "progression_trigger",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProgressionTriggerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val exerciseId: Long,
    val successfulSessionsNeeded: Int = 5,
    val minRirRequired: Int = 2,
    val weightIncrementKg: Double = 2.5,
    val progressionMode: ProgressionMode = ProgressionMode.LOAD
)
