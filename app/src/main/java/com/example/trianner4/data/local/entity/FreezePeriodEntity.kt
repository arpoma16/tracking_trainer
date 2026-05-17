package com.example.trianner4.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "freeze_period")
data class FreezePeriodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDate: Long,
    val endDate: Long?,
    val reason: String
)
