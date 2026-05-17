package com.example.trianner4.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.trianner4.data.local.ExerciseType
import com.example.trianner4.data.local.TrackingMode

@Entity(
    tableName = "exercise",
    foreignKeys = [
        ForeignKey(
            entity = BodyZoneEntity::class,
            parentColumns = ["id"],
            childColumns = ["primaryBodyZoneId"]
        ),
        ForeignKey(
            entity = BiomechanicalChainEntity::class,
            parentColumns = ["id"],
            childColumns = ["chainId"]
        )
    ]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: ExerciseType,
    val trackingMode: TrackingMode,
    val description: String = "",
    val mediaRef: String? = null,
    @ColumnInfo(index = true) val primaryBodyZoneId: Long? = null,
    @ColumnInfo(index = true) val chainId: Long? = null,
    val defaultSets: Int? = null,
    val defaultReps: Int? = null,
    val defaultRir: Int? = null,
    val defaultDurationSec: Int? = null
)
