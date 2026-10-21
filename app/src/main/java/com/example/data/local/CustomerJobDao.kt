package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustomerJobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerJobDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: CustomerJobEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<CustomerJobEntity>)

    @Update
    suspend fun updateJob(job: CustomerJobEntity)

    @Delete
    suspend fun deleteJob(job: CustomerJobEntity)

    @Query("SELECT * FROM customer_jobs WHERE isDeleted = 0 ORDER BY id DESC")
    fun getAllActiveJobs(): Flow<List<CustomerJobEntity>>

    @Query("SELECT * FROM customer_jobs WHERE isDeleted = 0 ORDER BY id DESC")
    fun getAllJobs(): Flow<List<CustomerJobEntity>>

    @Query("SELECT * FROM customer_jobs WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getRecycleBinJobs(): Flow<List<CustomerJobEntity>>

    @Query("SELECT * FROM customer_jobs WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedJobs(): Flow<List<CustomerJobEntity>>

    @Query("SELECT * FROM customer_jobs WHERE (assignedExpertId = :expertId OR assigned_technician_id = :expertId) AND isDeleted = 0 ORDER BY id DESC")
    fun getJobsForExpert(expertId: Long): Flow<List<CustomerJobEntity>>

    @Query("SELECT * FROM customer_jobs WHERE id = :id")
    suspend fun getJobById(id: Long): CustomerJobEntity?

    @Query("DELETE FROM customer_jobs WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("DELETE FROM customer_jobs WHERE id = :id")
    suspend fun deleteJobById(id: Long)

    @Query("DELETE FROM customer_jobs WHERE customerPhone = :phone")
    suspend fun deleteJobsByCustomerPhone(phone: String)

    @Query("DELETE FROM customer_jobs WHERE customerPhone IN ('9876543210', '9999999999') OR customerName LIKE '%Live verification%'")
    suspend fun deleteDeprecatedVerificationJobs()

    @Query("DELETE FROM customer_jobs WHERE isDeleted = 1")
    suspend fun clearRecycleBin()

    @Query("UPDATE customer_jobs SET isDeleted = 1, deletedAt = :timestamp, last_updated = :timestamp WHERE id = :jobId")
    suspend fun moveToRecycleBin(jobId: Long, timestamp: Long)

    @Query("UPDATE customer_jobs SET isDeleted = 0, deletedAt = NULL, last_updated = :timestamp WHERE id = :jobId")
    suspend fun restoreJobFromRecycleBin(jobId: Long, timestamp: Long)

    @Query("DELETE FROM customer_jobs WHERE isDeleted = 1 AND deletedAt <= :cutoff")
    suspend fun purgeJobsOlderThan(cutoff: Long)

    @Query("UPDATE customer_jobs SET status = 'PENDING', assignedExpertId = NULL, assignedExpertName = NULL, assignedExpertPhone = NULL, assigned_technician_id = NULL, assigned_technician_name = NULL, last_updated = :timestamp WHERE id = :jobId")
    suspend fun unassignExpertFromJob(jobId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE customer_jobs SET status = :status WHERE id = :jobId")
    suspend fun updateJobStatus(jobId: Long, status: String)

    @Query("UPDATE customer_jobs SET isExpertNotified = :sent WHERE id = :jobId")
    suspend fun updateExpertNotified(jobId: Long, sent: Boolean)

    @Query("UPDATE customer_jobs SET isCustomerNotifiedOnAssign = :sent WHERE id = :jobId")
    suspend fun updateCustomerNotifiedOnAssign(jobId: Long, sent: Boolean)

    @Query("UPDATE customer_jobs SET isCustomerNotifiedOnCompletion = :sent WHERE id = :jobId")
    suspend fun updateCustomerNotifiedOnCompletion(jobId: Long, sent: Boolean)

    @Query("UPDATE customer_jobs SET last_updated = :time WHERE id = :jobId")
    suspend fun updateMessageDismissedAt(jobId: Long, time: Long?)

    @Query("UPDATE customer_jobs SET status = :status, ratingGiven = :rating, reviewFeedback = :feedback, technicianBeforePhotoUrl = COALESCE(:beforeUrl, technicianBeforePhotoUrl), technicianAfterPhotoUrl = COALESCE(:afterUrl, technicianAfterPhotoUrl), completedAt = :completedAt, last_updated = :completedAt WHERE id = :jobId")
    suspend fun completeOrCancelJobWithReview(jobId: Long, status: String, rating: Float, feedback: String?, completedAt: Long, beforeUrl: String? = null, afterUrl: String? = null)

    @Query("UPDATE customer_jobs SET managed_by_user_id = :userId, managed_by_user_name = :userName, managed_by_designation = :designation, last_updated = :timestamp WHERE id = :jobId")
    suspend fun updateJobManager(jobId: Long, userId: String, userName: String, designation: String?, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM customer_jobs WHERE customerPhone = :phone AND createdAt >= :sinceTimestamp ORDER BY id DESC LIMIT 1")
    suspend fun findRecentOrderByPhone(phone: String, sinceTimestamp: Long): CustomerJobEntity?

    @Query("UPDATE customer_jobs SET status = :status, assignedExpertId = :expertId, assignedExpertName = :expertName, assignedExpertPhone = :expertPhone, distanceKmAtDispatch = :distanceKm, assigned_at_timestamp = :assignedAt WHERE id = :jobId")
    suspend fun updateJobDispatch(jobId: Long, status: String, expertId: Long, expertName: String, expertPhone: String, distanceKm: Double, assignedAt: Long = System.currentTimeMillis())

    @Query("UPDATE customer_jobs SET is_synced = 1 WHERE id = :jobId")
    suspend fun markJobSynced(jobId: Long)

    @Query("SELECT * FROM customer_jobs WHERE is_synced = 0")
    suspend fun getUnsyncedJobs(): List<CustomerJobEntity>

    @Query("SELECT MAX(id) FROM customer_jobs WHERE id < 1000000")
    suspend fun getMaxSequentialJobId(): Long?

    @Query("SELECT COUNT(*) FROM customer_jobs")
    suspend fun getJobCount(): Int

    @Query("SELECT * FROM customer_jobs ORDER BY createdAt ASC, id ASC")
    suspend fun getAllJobsRaw(): List<CustomerJobEntity>
}
