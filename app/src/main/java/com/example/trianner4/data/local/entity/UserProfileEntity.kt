package com.example.trianner4.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.trianner4.data.local.UnitSystem

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val birthDate: Long?,
    val unitSystem: UnitSystem = UnitSystem.KG,
    val defaultRestSec: Int = 90,
    val weeklyTargetSessions: Int,
    val createdAt: Long
)
