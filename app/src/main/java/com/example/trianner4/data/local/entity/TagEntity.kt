package com.example.trianner4.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.trianner4.data.local.TagCategory

@Entity(tableName = "tag")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: TagCategory
)
