package com.example.trianner4.data.backup

import com.google.gson.GsonBuilder
import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun execute(): String {
        val data = repository.getFullBackupData()
        return gson.toJson(data)
    }
}
