package com.example.trianner4.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "biomechanical_chain")
data class BiomechanicalChainEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)
