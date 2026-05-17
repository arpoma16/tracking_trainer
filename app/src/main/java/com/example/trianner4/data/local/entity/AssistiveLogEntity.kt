package com.example.trianner4.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "assistive_log",
    foreignKeys = [
        ForeignKey(
            entity = SessionExerciseSnapshotEntity::class,
            parentColumns = ["id"],
            childColumns = ["snapshotId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DiscomfortEntity::class,
            parentColumns = ["id"],
            childColumns = ["injectedByDiscomfortId"]
        )
    ]
)
data class AssistiveLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val snapshotId: Long,
    val durationActualSec: Int?,
    val repsActual: Int?,
    val completed: Boolean = false,
    val reliefScore: Int?,
    @ColumnInfo(index = true) val injectedByDiscomfortId: Long?
)
