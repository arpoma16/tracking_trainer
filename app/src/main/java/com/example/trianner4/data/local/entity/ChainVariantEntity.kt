package com.example.trianner4.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "chain_variant",
    foreignKeys = [
        ForeignKey(
            entity = BiomechanicalChainEntity::class,
            parentColumns = ["id"],
            childColumns = ["chainId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"]
        ),
        ForeignKey(
            entity = ChainVariantEntity::class,
            parentColumns = ["id"],
            childColumns = ["previousVariantId"]
        )
    ]
)
data class ChainVariantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val chainId: Long,
    val level: Int,
    @ColumnInfo(index = true) val exerciseId: Long,
    @ColumnInfo(index = true) val previousVariantId: Long?,
    val eligibleForPromotion: Boolean = false
)
