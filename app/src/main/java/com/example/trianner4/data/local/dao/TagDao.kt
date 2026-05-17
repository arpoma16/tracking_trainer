package com.example.trianner4.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.trianner4.data.local.TagCategory
import com.example.trianner4.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(tags: List<TagEntity>): List<Long>

    @Delete
    suspend fun delete(tag: TagEntity)

    @Query("SELECT * FROM tag ORDER BY category, name")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tag WHERE category = :category ORDER BY name")
    fun observeByCategory(category: TagCategory): Flow<List<TagEntity>>

    @Query("SELECT * FROM tag WHERE id = :id")
    suspend fun getById(id: Long): TagEntity?

    /** Tags de un ejercicio concreto (JOIN inverso para la UI de edición). */
    @Query("""
        SELECT t.* FROM tag t
        INNER JOIN exercise_tag et ON et.tagId = t.id
        WHERE et.exerciseId = :exerciseId
        ORDER BY t.category, t.name
    """)
    suspend fun getTagsForExercise(exerciseId: Long): List<TagEntity>

    /** Tags de una molestia concreta (JOIN inverso para la UI de Bienestar). */
    @Query("""
        SELECT t.* FROM tag t
        INNER JOIN discomfort_tag dt ON dt.tagId = t.id
        WHERE dt.discomfortId = :discomfortId
        ORDER BY t.category, t.name
    """)
    suspend fun getTagsForDiscomfort(discomfortId: Long): List<TagEntity>
}
