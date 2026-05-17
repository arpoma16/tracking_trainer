package com.example.trianner4.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trianner4.data.local.entity.DiscomfortEntity
import com.example.trianner4.data.local.entity.DiscomfortTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscomfortDao {

    // ── CRUD ───────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(discomfort: DiscomfortEntity): Long

    @Update
    suspend fun update(discomfort: DiscomfortEntity)

    @Delete
    suspend fun delete(discomfort: DiscomfortEntity)

    @Query("SELECT * FROM discomfort WHERE id = :id")
    suspend fun getById(id: Long): DiscomfortEntity?

    // ── Lecturas ───────────────────────────────────────────────────────────────

    /** Molestias activas que el resolver de adaptación debe evaluar. */
    @Query("SELECT * FROM discomfort WHERE isActive = 1 ORDER BY severity DESC, startedAt DESC")
    fun observeActive(): Flow<List<DiscomfortEntity>>

    /** Historial completo para la pantalla Bienestar. */
    @Query("SELECT * FROM discomfort ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<DiscomfortEntity>>

    // ── Cambios de estado ──────────────────────────────────────────────────────

    /** Marca la molestia como resuelta sin exponer setters directos. */
    @Query("UPDATE discomfort SET isActive = 0, resolvedAt = :resolvedAt WHERE id = :id")
    suspend fun resolve(id: Long, resolvedAt: Long)

    @Query("UPDATE discomfort SET isActive = 1, resolvedAt = NULL WHERE id = :id")
    suspend fun reactivate(id: Long)

    // ── Tags de molestia ───────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDiscomfortTag(tag: DiscomfortTagEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDiscomfortTags(tags: List<DiscomfortTagEntity>)

    @Delete
    suspend fun deleteDiscomfortTag(tag: DiscomfortTagEntity)

    @Query("SELECT * FROM discomfort_tag WHERE discomfortId = :discomfortId")
    suspend fun getTagsForDiscomfort(discomfortId: Long): List<DiscomfortTagEntity>

    @Query("DELETE FROM discomfort_tag WHERE discomfortId = :discomfortId")
    suspend fun clearTagsForDiscomfort(discomfortId: Long)
}
