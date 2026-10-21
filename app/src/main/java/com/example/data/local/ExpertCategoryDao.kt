package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExpertCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpertCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: ExpertCategoryEntity): Long

    @Update
    suspend fun updateCategory(category: ExpertCategoryEntity)

    @Query("SELECT * FROM expert_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<ExpertCategoryEntity>>

    @Delete
    suspend fun deleteCategory(category: ExpertCategoryEntity)

    @Query("DELETE FROM expert_categories WHERE id = :id")
    suspend fun deleteCategory(id: Long)

    @Query("DELETE FROM expert_categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Long)

    @Query("DELETE FROM expert_categories WHERE name = :name")
    suspend fun deleteCategoryByName(name: String)

    @Query("DELETE FROM expert_categories WHERE name NOT IN ('Electrician', 'Plumber')")
    suspend fun deleteNonDefaultCategories()

    @Query("DELETE FROM expert_categories WHERE isDefault = 1 AND name NOT IN ('Electrician', 'Plumber')")
    suspend fun deleteOtherDefaultCategories()

    @Query("SELECT COUNT(*) FROM expert_categories")
    suspend fun getCategoryCount(): Int

    @Query("UPDATE expert_categories SET is_synced = 1 WHERE id = :id")
    suspend fun markCategorySynced(id: Long)

    @Query("SELECT * FROM expert_categories WHERE is_synced = 0")
    suspend fun getUnsyncedCategories(): List<ExpertCategoryEntity>
}
