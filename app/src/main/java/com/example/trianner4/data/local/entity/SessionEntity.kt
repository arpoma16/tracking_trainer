package com.example.trianner4.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.trianner4.data.local.SessionStatus

@Entity(
    tableName = "session",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"]
        )
    ]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val routineId: Long,
    val date: Long,
    val startedAt: Long,
    val endedAt: Long?,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val totalTonnage: Double?,
    val avgRir: Double?,
    val fatigueScore: Int?,
    val painScore: Int?,
    val readinessScore: Int?,
    val isAdapted: Boolean = false,
    val isDeload: Boolean = false
)
