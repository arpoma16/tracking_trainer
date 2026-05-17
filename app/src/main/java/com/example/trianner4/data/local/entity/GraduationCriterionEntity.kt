package com.example.trianner4.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "graduation_criterion",
    foreignKeys = [
        ForeignKey(
            entity = ChainVariantEntity::class,
            parentColumns = ["id"],
            childColumns = ["chainVariantId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GraduationCriterionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val chainVariantId: Long,
    val requiredSuccessfulSessions: Int = 5,
    val requiredMinRir: Int = 2,
    val requireFullRom: Boolean = true,
    val requireZeroPain: Boolean = true,
    val manualConfirmed: Boolean = false
)
