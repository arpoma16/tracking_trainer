package com.example.trianner4.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "set_log",
    foreignKeys = [
        ForeignKey(
            entity = SessionExerciseSnapshotEntity::class,
            parentColumns = ["id"],
            childColumns = ["snapshotId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SetLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val snapshotId: Long,
    val setIndex: Int,
    val weightKg: Double?,
    val bandTension: String?,
    val reps: Int,
    val rir: Int,
    val isCompleted: Boolean = false,
    val isPr: Boolean = false
)
