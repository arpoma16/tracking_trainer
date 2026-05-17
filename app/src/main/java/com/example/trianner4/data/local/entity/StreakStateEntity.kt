package com.example.trianner4.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streak_state")
data class StreakStateEntity(
    @PrimaryKey val id: Int = 1,
    val weeklyConsistencyCount: Int = 0,
    val weeklyConsistencyAnchorWeek: Int = 0,
    val routineAdherenceCount: Int = 0,
    val routineAdherenceAnchorDate: Long = 0L,
    val lastUpdated: Long
)
