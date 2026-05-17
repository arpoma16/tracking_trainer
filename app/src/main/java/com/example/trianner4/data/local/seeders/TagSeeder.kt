package com.example.trianner4.data.local.seeders

import com.example.trianner4.data.local.TagCategory
import com.example.trianner4.data.local.dao.TagDao
import com.example.trianner4.data.local.entity.TagEntity
import kotlinx.coroutines.flow.first

object TagSeeder {
    suspend fun seed(tagDao: TagDao) {
        val existingTags = tagDao.observeAll().first()
        if (existingTags.isNotEmpty()) return

        val tags = listOf(
            // PATTERN
            TagEntity(name = "Empuje Horizontal", category = TagCategory.PATTERN),
            TagEntity(name = "Empuje Vertical", category = TagCategory.PATTERN),
            TagEntity(name = "Tracción Horizontal", category = TagCategory.PATTERN),
            TagEntity(name = "Tracción Vertical", category = TagCategory.PATTERN),
            TagEntity(name = "Dominante de Rodilla", category = TagCategory.PATTERN),
            TagEntity(name = "Dominante de Cadera", category = TagCategory.PATTERN),
            
            // BODY
            TagEntity(name = "Tren Superior", category = TagCategory.BODY),
            TagEntity(name = "Tren Inferior", category = TagCategory.BODY),
            TagEntity(name = "Core", category = TagCategory.BODY),
            
            // JOINT
            TagEntity(name = "Hombro", category = TagCategory.JOINT),
            TagEntity(name = "Codo", category = TagCategory.JOINT),
            TagEntity(name = "Muñeca", category = TagCategory.JOINT),
            TagEntity(name = "Cadera", category = TagCategory.JOINT),
            TagEntity(name = "Rodilla", category = TagCategory.JOINT),
            TagEntity(name = "Tobillo", category = TagCategory.JOINT),
            
            // MUSCLE
            TagEntity(name = "Pectoral", category = TagCategory.MUSCLE),
            TagEntity(name = "Dorsal", category = TagCategory.MUSCLE),
            TagEntity(name = "Deltoides", category = TagCategory.MUSCLE),
            TagEntity(name = "Bíceps", category = TagCategory.MUSCLE),
            TagEntity(name = "Tríceps", category = TagCategory.MUSCLE),
            TagEntity(name = "Cuádriceps", category = TagCategory.MUSCLE),
            TagEntity(name = "Isquiotibiales", category = TagCategory.MUSCLE),
            TagEntity(name = "Glúteo", category = TagCategory.MUSCLE),
            TagEntity(name = "Gemelo", category = TagCategory.MUSCLE),
            
            // GOAL
            TagEntity(name = "Fuerza", category = TagCategory.GOAL),
            TagEntity(name = "Hipertrofia", category = TagCategory.GOAL),
            TagEntity(name = "Resistencia", category = TagCategory.GOAL),
            TagEntity(name = "Movilidad", category = TagCategory.GOAL)
        )
        tagDao.insertAll(tags)
    }
}
