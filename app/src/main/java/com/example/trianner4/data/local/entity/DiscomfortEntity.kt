package com.example.trianner4.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "discomfort",
    foreignKeys = [
        ForeignKey(
            entity = BodyZoneEntity::class,
            parentColumns = ["id"],
            childColumns = ["bodyZoneId"]
        )
    ]
)
data class DiscomfortEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val bodyZoneId: Long,
    val label: String,
    val freeText: String?,
    val severity: Int,
    val startedAt: Long,
    val resolvedAt: Long?,
    val isActive: Boolean = true
)
