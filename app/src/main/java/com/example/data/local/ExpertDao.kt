package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExpertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpertDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpert(expert: ExpertEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperts(experts: List<ExpertEntity>)

    @Update
    suspend fun updateExpert(expert: ExpertEntity)

    @Delete
    suspend fun deleteExpert(expert: ExpertEntity)

    @Query("SELECT * FROM experts WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllActiveExperts(): Flow<List<ExpertEntity>>

    @Query("SELECT * FROM experts WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllExperts(): Flow<List<ExpertEntity>>

    @Query("SELECT * FROM experts WHERE isDeleted = 0 AND isAvailable = 1 ORDER BY name ASC")
    fun getAvailableExperts(): Flow<List<ExpertEntity>>

    @Query("SELECT * FROM experts WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getRecycleBinExperts(): Flow<List<ExpertEntity>>

    @Query("SELECT * FROM experts WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedExperts(): Flow<List<ExpertEntity>>

    @Query("SELECT * FROM experts WHERE id = :id")
    suspend fun getExpertById(id: Long): ExpertEntity?

    @Query("DELETE FROM experts WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("DELETE FROM experts WHERE id = :id")
    suspend fun deleteExpertById(id: Long)

    @Query("DELETE FROM experts WHERE phone = :phone")
    suspend fun deleteExpertsByPhone(phone: String)

    @Query("DELETE FROM experts WHERE phone IN ('9876543210', '9999999999') OR name LIKE '%Live verification%'")
    suspend fun deleteDeprecatedVerificationExperts()

    @Query("DELETE FROM experts WHERE isDeleted = 1")
    suspend fun clearRecycleBin()

    @Query("UPDATE experts SET isDeleted = 1, deletedAt = :timestamp, last_updated = :timestamp WHERE id = :expertId")
    suspend fun moveToRecycleBin(expertId: Long, timestamp: Long)

    @Query("UPDATE experts SET isDeleted = 0, deletedAt = NULL, last_updated = :timestamp WHERE id = :expertId")
    suspend fun restoreExpertFromRecycleBin(expertId: Long, timestamp: Long)

    @Query("DELETE FROM experts WHERE isDeleted = 1 AND deletedAt <= :cutoff")
    suspend fun purgeExpertsOlderThan(cutoff: Long)

    @Query("UPDATE experts SET isWelcomeMessageSent = :sent WHERE id = :id")
    suspend fun updateWelcomeMessageSent(id: Long, sent: Boolean)

    @Query("UPDATE experts SET is_synced = 1 WHERE id = :id")
    suspend fun markExpertSynced(id: Long)

    @Query("SELECT * FROM experts WHERE is_synced = 0")
    suspend fun getUnsyncedExperts(): List<ExpertEntity>
}
