package com.example.trianner4.data.local.seeders

import com.example.trianner4.data.local.dao.BodyZoneDao
import com.example.trianner4.data.local.entity.BodyZoneEntity
import kotlinx.coroutines.flow.first

object BodyZoneSeeder {
    suspend fun seed(bodyZoneDao: BodyZoneDao) {
        val existingZones = bodyZoneDao.getAll().first()
        if (existingZones.isEmpty()) {
            val zones = listOf(
                BodyZoneEntity(parentId = null, level = 0, name = "Cabeza"),
                BodyZoneEntity(parentId = null, level = 0, name = "Cuello y Hombros"),
                BodyZoneEntity(parentId = null, level = 0, name = "Tronco"),
                BodyZoneEntity(parentId = null, level = 0, name = "Extremidades Superiores"),
                BodyZoneEntity(parentId = null, level = 0, name = "Extremidades Inferiores"),
                BodyZoneEntity(parentId = null, level = 0, name = "Espalda"),
                BodyZoneEntity(parentId = null, level = 0, name = "Pélvis y Glúteos")
            )
            bodyZoneDao.insertAll(zones)
        }
    }
}
