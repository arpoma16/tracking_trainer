package com.example.trianner4.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.trianner4.data.local.Phase

@Entity(
    tableName = "routine_phase_exercise",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"]
        ),
        ForeignKey(
            entity = ChainVariantEntity::class,
            parentColumns = ["id"],
            childColumns = ["chainVariantId"]
        )
    ]
)
data class RoutinePhaseExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val routineId: Long,
    val phase: Phase,
    @ColumnInfo(index = true) val exerciseId: Long,
    val orderIndex: Int,
    val targetSets: Int?,
    val targetReps: Int?,
    val targetRir: Int?,
    val targetDurationSec: Int?,
    val restSec: Int?,
    @ColumnInfo(index = true) val chainVariantId: Long?
)
