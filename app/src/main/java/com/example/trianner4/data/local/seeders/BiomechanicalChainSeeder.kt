package com.example.trianner4.data.local.seeders

import com.example.trianner4.data.local.dao.BiomechanicalChainDao
import com.example.trianner4.data.local.entity.BiomechanicalChainEntity
import kotlinx.coroutines.flow.first

object BiomechanicalChainSeeder {
    suspend fun seed(dao: BiomechanicalChainDao) {
        val existing = dao.observeAll().first()
        if (existing.isEmpty()) {
            val chains = listOf(
                BiomechanicalChainEntity(name = "Empuje Horizontal"),
                BiomechanicalChainEntity(name = "Empuje Vertical"),
                BiomechanicalChainEntity(name = "Tracción Horizontal"),
                BiomechanicalChainEntity(name = "Tracción Vertical"),
                BiomechanicalChainEntity(name = "Dominante de Rodilla"),
                BiomechanicalChainEntity(name = "Dominante de Cadera"),
                BiomechanicalChainEntity(name = "Core")
            )
            dao.insertAll(chains)
        }
    }
}
