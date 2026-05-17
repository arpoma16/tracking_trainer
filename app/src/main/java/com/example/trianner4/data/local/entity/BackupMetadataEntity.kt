package com.example.trianner4.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backup_metadata")
data class BackupMetadataEntity(
    @PrimaryKey val id: Int = 1,
    val lastExportAt: Long?,
    val lastExportPath: String?,
    val schemaVersion: Int
)
