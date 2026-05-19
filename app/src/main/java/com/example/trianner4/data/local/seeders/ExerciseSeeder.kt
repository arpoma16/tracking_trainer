package com.example.trianner4.data.local.seeders

import com.example.trianner4.data.local.ExerciseType
import com.example.trianner4.data.local.TrackingMode
import com.example.trianner4.data.local.dao.BiomechanicalChainDao
import com.example.trianner4.data.local.dao.BodyZoneDao
import com.example.trianner4.data.local.dao.ExerciseDao
import com.example.trianner4.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.first

object ExerciseSeeder {

    suspend fun seed(
        exerciseDao: ExerciseDao,
        bodyZoneDao: BodyZoneDao,
        biomechanicalChainDao: BiomechanicalChainDao
    ) {
        val existing = exerciseDao.observeAll().first()
        if (existing.isNotEmpty()) return

        BodyZoneSeeder.seed(bodyZoneDao)
        BiomechanicalChainSeeder.seed(biomechanicalChainDao)

        val zones = bodyZoneDao.getAll().first().associateBy { it.name }
        val chains = biomechanicalChainDao.observeAll().first().associateBy { it.name }

        val exercises = listOf(
            ExerciseEntity(
                name = "Press de Banca",
                type = ExerciseType.STRENGTH,
                trackingMode = TrackingMode.WEIGHT_REPS,
                description = "Empuje horizontal con barra en banco plano",
                primaryBodyZoneId = zones["Tronco"]?.id,
                chainId = chains["Empuje Horizontal"]?.id,
                defaultSets = 4,
                defaultReps = 8,
                defaultRir = 2
            ),
            ExerciseEntity(
                name = "Sentadilla",
                type = ExerciseType.STRENGTH,
                trackingMode = TrackingMode.WEIGHT_REPS,
                description = "Sentadilla con barra en rack",
                primaryBodyZoneId = zones["Extremidades Inferiores"]?.id,
                chainId = chains["Dominante de Rodilla"]?.id,
                defaultSets = 4,
                defaultReps = 6,
                defaultRir = 2
            ),
            ExerciseEntity(
                name = "Peso Muerto",
                type = ExerciseType.STRENGTH,
                trackingMode = TrackingMode.WEIGHT_REPS,
                description = "Peso muerto convencional con barra",
                primaryBodyZoneId = zones["Espalda"]?.id,
                chainId = chains["Dominante de Cadera"]?.id,
                defaultSets = 3,
                defaultReps = 5,
                defaultRir = 2
            ),
            ExerciseEntity(
                name = "Jalón al Pecho",
                type = ExerciseType.STRENGTH,
                trackingMode = TrackingMode.WEIGHT_REPS,
                description = "Tracción vertical en polea alta",
                primaryBodyZoneId = zones["Espalda"]?.id,
                chainId = chains["Tracción Vertical"]?.id,
                defaultSets = 3,
                defaultReps = 10,
                defaultRir = 2
            ),
            ExerciseEntity(
                name = "Press Militar",
                type = ExerciseType.STRENGTH,
                trackingMode = TrackingMode.WEIGHT_REPS,
                description = "Press sobre cabeza con barra de pie",
                primaryBodyZoneId = zones["Cuello y Hombros"]?.id,
                chainId = chains["Empuje Vertical"]?.id,
                defaultSets = 3,
                defaultReps = 8,
                defaultRir = 2
            )
        )

        exerciseDao.insertAll(exercises)
    }
}
