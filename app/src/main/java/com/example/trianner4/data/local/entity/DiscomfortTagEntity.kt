package com.example.trianner4.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "discomfort_tag",
    primaryKeys = ["discomfortId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = DiscomfortEntity::class,
            parentColumns = ["id"],
            childColumns = ["discomfortId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DiscomfortTagEntity(
    val discomfortId: Long,
    @ColumnInfo(index = true) val tagId: Long
)
