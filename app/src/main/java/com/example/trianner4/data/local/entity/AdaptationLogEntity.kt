package com.example.trianner4.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.trianner4.data.local.AdaptationActionType

@Entity(
    tableName = "adaptation_log",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DiscomfortEntity::class,
            parentColumns = ["id"],
            childColumns = ["discomfortId"]
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["affectedExerciseId"]
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["replacementExerciseId"]
        )
    ]
)
data class AdaptationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val sessionId: Long,
    @ColumnInfo(index = true) val discomfortId: Long,
    val actionType: AdaptationActionType,
    @ColumnInfo(index = true) val affectedExerciseId: Long?,
    @ColumnInfo(index = true) val replacementExerciseId: Long?,
    val loadFactor: Double?
)
