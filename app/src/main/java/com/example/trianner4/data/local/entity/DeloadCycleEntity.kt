package com.example.trianner4.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deload_cycle")
data class DeloadCycleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDate: Long,
    val endDate: Long,
    val triggerReason: String,
    val loadFactor: Double = 0.6
)
