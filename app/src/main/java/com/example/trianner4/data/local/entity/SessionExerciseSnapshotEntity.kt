package com.example.trianner4.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.trianner4.data.local.Phase

@Entity(
    tableName = "session_exercise_snapshot",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"]
        )
    ]
)
data class SessionExerciseSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val sessionId: Long,
    val phase: Phase,
    @ColumnInfo(index = true) val exerciseId: Long,
    val exerciseNameSnapshot: String,
    val chainVariantLevelSnapshot: Int?,
    val wasSubstituted: Boolean = false,
    val substitutionReason: String?,
    val orderIndex: Int
)
