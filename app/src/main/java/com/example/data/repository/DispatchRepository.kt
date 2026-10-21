package com.example.data.repository

import android.content.Context
import com.example.data.firebase.FirestoreSyncManager
import com.example.data.local.AppDatabase
import com.example.data.local.CustomerDao
import com.example.data.local.CustomerJobDao
import com.example.data.local.ExpertCategoryDao
import com.example.data.local.ExpertDao
import com.example.data.local.TechnicianDao
import com.example.data.model.CustomerEntity
import com.example.data.model.CustomerJobEntity
import com.example.data.model.ExpertCategoryEntity
import com.example.data.model.ExpertEntity
import com.example.data.model.JobStatus
import com.example.data.model.RankedExpert
import com.example.data.model.TechnicianEntity
import com.example.util.LocationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class DispatchRepository(
    private val context: Context? = null,
    initialUserPhone: String = "",
    private val fallbackExpertDao: ExpertDao? = null,
    private val fallbackJobDao: CustomerJobDao? = null,
    private val fallbackCategoryDao: ExpertCategoryDao? = null,
    private val fallbackTechnicianDao: TechnicianDao? = null,
    private val fallbackCustomerDao: CustomerDao? = null
) {
    // Secondary constructor for direct DAO usage / unit tests
    constructor(
        expertDao: ExpertDao,
        jobDao: CustomerJobDao,
        expertCategoryDao: ExpertCategoryDao,
        technicianDao: TechnicianDao? = null,
        customerDao: CustomerDao? = null
    ) : this(
        context = null,
        initialUserPhone = "test_user",
        fallbackExpertDao = expertDao,
        fallbackJobDao = jobDao,
        fallbackCategoryDao = expertCategoryDao,
        fallbackTechnicianDao = technicianDao,
        fallbackCustomerDao = customerDao
    )

    private val _currentUserPhone = MutableStateFlow(initialUserPhone.replace(Regex("[^0-9]"), ""))
    val currentUserPhone: StateFlow<String> = _currentUserPhone.asStateFlow()

    fun setCurrentUser(phone: String) {
        _currentUserPhone.value = phone.replace(Regex("[^0-9]"), "")
    }

    private fun getDb(): AppDatabase? {
        val ctx = context ?: return null
        return AppDatabase.getDatabase(ctx)
    }

    private fun getExpertDao(): ExpertDao {
        return getDb()?.expertDao() ?: fallbackExpertDao ?: throw IllegalStateException("No ExpertDao available")
    }

    private fun getJobDao(): CustomerJobDao {
        return getDb()?.customerJobDao() ?: fallbackJobDao ?: throw IllegalStateException("No CustomerJobDao available")
    }

    private fun getCategoryDao(): ExpertCategoryDao {
        return getDb()?.expertCategoryDao() ?: fallbackCategoryDao ?: throw IllegalStateException("No CategoryDao available")
    }

    private fun getTechnicianDao(): TechnicianDao? {
        return getDb()?.technicianDao() ?: fallbackTechnicianDao
    }

    private fun getCustomerDao(): CustomerDao? {
        return getDb()?.customerDao() ?: fallbackCustomerDao
    }

    // Centralized shared flows across all users (Admin & Staff)
    val allExperts: Flow<List<ExpertEntity>> =
        (if (context != null) AppDatabase.getDatabase(context).expertDao().getAllExperts()
        else fallbackExpertDao?.getAllExperts() ?: flowOf(emptyList()))
            .map { list -> list.filter { !it.isDeleted } }

    val availableExperts: Flow<List<ExpertEntity>> =
        (if (context != null) AppDatabase.getDatabase(context).expertDao().getAvailableExperts()
        else fallbackExpertDao?.getAvailableExperts() ?: flowOf(emptyList()))
            .map { list -> list.filter { !it.isDeleted } }

    val allJobs: Flow<List<CustomerJobEntity>> =
        (if (context != null) AppDatabase.getDatabase(context).customerJobDao().getAllJobs()
        else fallbackJobDao?.getAllJobs() ?: flowOf(emptyList()))
            .map { list -> list.filter { !it.isDeleted } }

    val allCategories: Flow<List<ExpertCategoryEntity>> =
        if (context != null) AppDatabase.getDatabase(context).expertCategoryDao().getAllCategories()
        else fallbackCategoryDao?.getAllCategories() ?: flowOf(emptyList())

    val deletedJobs: Flow<List<CustomerJobEntity>> =
        if (context != null) AppDatabase.getDatabase(context).customerJobDao().getDeletedJobs()
        else fallbackJobDao?.getDeletedJobs() ?: flowOf(emptyList())

    val deletedExperts: Flow<List<ExpertEntity>> =
        if (context != null) AppDatabase.getDatabase(context).expertDao().getDeletedExperts()
        else fallbackExpertDao?.getDeletedExperts() ?: flowOf(emptyList())

    val allTechnicians: Flow<List<TechnicianEntity>>? =
        if (context != null) AppDatabase.getDatabase(context).technicianDao().getAllTechnicians()
        else fallbackTechnicianDao?.getAllTechnicians()

    val allCustomers: Flow<List<CustomerEntity>>? =
        if (context != null) AppDatabase.getDatabase(context).customerDao().getAllCustomers()
        else fallbackCustomerDao?.getAllCustomers()

    fun getJobsForExpert(expertId: Long): Flow<List<CustomerJobEntity>> =
        if (context != null) AppDatabase.getDatabase(context).customerJobDao().getJobsForExpert(expertId)
        else fallbackJobDao?.getJobsForExpert(expertId) ?: flowOf(emptyList())

    // -----------------------------------------------------------------
    // Duplicate Order Protection (Last 4 Hours Check)
    // -----------------------------------------------------------------

    suspend fun checkDuplicateRecentOrder(customerPhone: String): Boolean {
        if (context != null) {
            return FirestoreSyncManager.getInstance(context).checkDuplicateRecentOrder(customerPhone)
        }
        val clean = customerPhone.replace(Regex("[^0-9]"), "")
        val fourHoursAgo = System.currentTimeMillis() - (4 * 60 * 60 * 1000L)
        return getJobDao().findRecentOrderByPhone(clean, fourHoursAgo) != null
    }

    // -----------------------------------------------------------------
    // Real-Time Order Assignment Lock
    // -----------------------------------------------------------------

    suspend fun assignJobToExpertWithLock(
        jobId: Long,
        expert: ExpertEntity,
        distanceKm: Double,
        managedByUserId: String? = null,
        managedByUserName: String? = null,
        managedByDesignation: String? = null
    ): Result<Unit> {
        if (context != null) {
            return FirestoreSyncManager.getInstance(context).assignExpertWithTransaction(
                jobId = jobId,
                expertId = expert.id,
                expertName = expert.name,
                expertPhone = expert.phone,
                distanceKm = distanceKm,
                managedByUserId = managedByUserId,
                managedByUserName = managedByUserName,
                managedByDesignation = managedByDesignation
            )
        } else {
            getJobDao().updateJobDispatch(
                jobId = jobId,
                status = JobStatus.PROCESSING.name,
                expertId = expert.id,
                expertName = expert.name,
                expertPhone = expert.phone,
                distanceKm = distanceKm
            )
            if (!managedByUserId.isNullOrBlank() && !managedByUserName.isNullOrBlank()) {
                getJobDao().updateJobManager(jobId, managedByUserId, managedByUserName, managedByDesignation)
            }
            return Result.success(Unit)
        }
    }

    suspend fun takeoverOrder(jobId: Long, userId: String, userName: String, userDesignation: String? = null): Result<Unit> {
        if (context != null) {
            return FirestoreSyncManager.getInstance(context).takeoverOrder(jobId, userId, userName, userDesignation)
        } else {
            getJobDao().updateJobManager(jobId, userId, userName, userDesignation)
            return Result.success(Unit)
        }
    }

    // -----------------------------------------------------------------
    // Standard CRUD Operations
    // -----------------------------------------------------------------

    suspend fun moveJobToRecycleBin(jobId: Long) {
        val now = System.currentTimeMillis()
        getJobDao().moveToRecycleBin(jobId, now)
        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            syncManager.scope.launch {
                try {
                    val firestore = syncManager.getFirestore()
                    firestore?.collection(FirestoreSyncManager.JOBS_COLLECTION)?.document("job_$jobId")?.update(
                        mapOf(
                            "isDeleted" to true,
                            "deletedAt" to now,
                            "last_updated" to now
                        )
                    )?.await()
                } catch (_: Exception) {}
                syncManager.syncNow()
            }
        }
    }

    suspend fun restoreJobFromRecycleBin(jobId: Long) {
        val now = System.currentTimeMillis()
        getJobDao().restoreJobFromRecycleBin(jobId, now)
        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            syncManager.scope.launch {
                try {
                    val firestore = syncManager.getFirestore()
                    firestore?.collection(FirestoreSyncManager.JOBS_COLLECTION)?.document("job_$jobId")?.update(
                        mapOf(
                            "isDeleted" to false,
                            "deletedAt" to null,
                            "last_updated" to now
                        )
                    )?.await()
                } catch (_: Exception) {}
                syncManager.syncNow()
            }
        }
    }

    suspend fun deleteJobPermanently(jobId: Long) {
        getJobDao().deleteJobById(jobId)
        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            syncManager.scope.launch {
                try {
                    val firestore = syncManager.getFirestore()
                    firestore?.collection(FirestoreSyncManager.JOBS_COLLECTION)?.document("job_$jobId")?.delete()?.await()
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun moveExpertToRecycleBin(expertId: Long) {
        val now = System.currentTimeMillis()
        getExpertDao().moveToRecycleBin(expertId, now)
        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            syncManager.scope.launch {
                try {
                    val firestore = syncManager.getFirestore()
                    firestore?.collection(FirestoreSyncManager.EXPERTS_COLLECTION)?.document("expert_$expertId")?.update(
                        mapOf(
                            "isDeleted" to true,
                            "deletedAt" to now,
                            "last_updated" to now
                        )
                    )?.await()
                } catch (_: Exception) {}
                syncManager.syncNow()
            }
        }
    }

    suspend fun restoreExpertFromRecycleBin(expertId: Long) {
        val now = System.currentTimeMillis()
        getExpertDao().restoreExpertFromRecycleBin(expertId, now)
        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            syncManager.scope.launch {
                try {
                    val firestore = syncManager.getFirestore()
                    firestore?.collection(FirestoreSyncManager.EXPERTS_COLLECTION)?.document("expert_$expertId")?.update(
                        mapOf(
                            "isDeleted" to false,
                            "deletedAt" to null,
                            "last_updated" to now
                        )
                    )?.await()
                } catch (_: Exception) {}
                syncManager.syncNow()
            }
        }
    }

    suspend fun deleteExpertPermanently(expertId: Long) {
        getExpertDao().deleteExpertById(expertId)
        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            syncManager.scope.launch {
                try {
                    val firestore = syncManager.getFirestore()
                    firestore?.collection(FirestoreSyncManager.EXPERTS_COLLECTION)?.document("expert_$expertId")?.delete()?.await()
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun purgeRecycleBinOlderThan30Days() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        getJobDao().purgeJobsOlderThan(thirtyDaysAgo)
        getExpertDao().purgeExpertsOlderThan(thirtyDaysAgo)
    }

    suspend fun clearRecycleBin() {
        getJobDao().clearRecycleBin()
        getExpertDao().clearRecycleBin()
    }

    suspend fun updateWelcomeMessageSent(expertId: Long, sent: Boolean) {
        getExpertDao().updateWelcomeMessageSent(expertId, sent)
    }

    // Category operations
    suspend fun insertCategory(name: String, isDefault: Boolean = false): Long {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return 0L
        val cat = ExpertCategoryEntity(
            name = trimmed,
            isDefault = isDefault,
            last_updated = System.currentTimeMillis()
        )
        val id = getCategoryDao().insertCategory(cat)
        val finalCat = cat.copy(id = id)
        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            syncManager.scope.launch {
                try {
                    syncManager.getFirestore()?.collection(FirestoreSyncManager.CATEGORIES_COLLECTION)
                        ?.document("cat_$id")
                        ?.set(mapOf(
                            "id" to finalCat.id,
                            "name" to finalCat.name,
                            "isDefault" to finalCat.isDefault,
                            "createdAt" to finalCat.createdAt,
                            "last_updated" to finalCat.last_updated
                        ), com.google.firebase.firestore.SetOptions.merge())?.await()
                } catch (_: Exception) {}
                syncManager.syncNow()
            }
        }
        return id
    }

    suspend fun deleteCategory(category: ExpertCategoryEntity) {
        getCategoryDao().deleteCategory(category)
        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            syncManager.scope.launch {
                try {
                    syncManager.getFirestore()?.collection(FirestoreSyncManager.CATEGORIES_COLLECTION)
                        ?.document("cat_${category.id}")?.delete()?.await()
                } catch (_: Exception) {}
                syncManager.syncNow()
            }
        }
    }

    suspend fun deleteCategoryByName(name: String) {
        getCategoryDao().deleteCategoryByName(name)
    }

    // Technician operations
    suspend fun insertTechnician(technician: TechnicianEntity): Long {
        val id = getTechnicianDao()?.insertTechnician(technician) ?: 0L
        getExpertDao().insertExpert(
            ExpertEntity(
                name = technician.name,
                phone = technician.contact,
                category = technician.category,
                address = technician.address,
                latitude = technician.latitude,
                longitude = technician.longitude,
                isAvailable = technician.isAvailable,
                rating = technician.rating,
                completedJobsCount = technician.completedJobsCount,
                last_updated = System.currentTimeMillis()
            )
        )
        return id
    }

    suspend fun updateTechnician(technician: TechnicianEntity) {
        getTechnicianDao()?.updateTechnician(technician)
    }

    suspend fun deleteTechnician(technician: TechnicianEntity) {
        getTechnicianDao()?.deleteTechnician(technician)
    }

    suspend fun getTechnicianById(id: Long): TechnicianEntity? = getTechnicianDao()?.getTechnicianById(id)

    // Customer operations
    suspend fun insertCustomer(customer: CustomerEntity): Long =
        getCustomerDao()?.insertCustomer(customer) ?: 0L

    suspend fun updateCustomer(customer: CustomerEntity) {
        getCustomerDao()?.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) {
        getCustomerDao()?.deleteCustomer(customer)
    }

    suspend fun getCustomerById(id: Long): CustomerEntity? = getCustomerDao()?.getCustomerById(id)

    // Expert operations
    suspend fun insertExpert(expert: ExpertEntity): Long {
        val uniqueId = if (expert.id > 0) expert.id else (System.currentTimeMillis() * 1000L + (100..999).random())
        val now = System.currentTimeMillis()
        val updated = expert.copy(id = uniqueId, last_updated = now)
        val id = getExpertDao().insertExpert(updated)
        val finalExpert = updated.copy(id = if (id > 0) id else uniqueId)
        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            try {
                syncManager.pushExpertDirectly(finalExpert)
            } catch (e: Exception) {
                android.util.Log.e("DispatchRepository", "Push expert direct error: ${e.message}", e)
            }
            syncManager.scope.launch {
                syncManager.syncNow()
            }
        }
        return if (id > 0) id else uniqueId
    }

    suspend fun updateExpert(expert: ExpertEntity) {
        val updated = expert.copy(last_updated = System.currentTimeMillis())
        getExpertDao().updateExpert(updated)
        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            try {
                syncManager.pushExpertDirectly(updated)
            } catch (_: Exception) {}
            syncManager.scope.launch {
                syncManager.syncNow()
            }
        }
    }

    suspend fun deleteExpert(expert: ExpertEntity) = getExpertDao().deleteExpert(expert)

    // Customer Job / Order operations
    suspend fun insertJob(job: CustomerJobEntity): Long {
        val nextId = if (job.id in 1..999999) {
            job.id
        } else {
            val maxSeq = getJobDao().getMaxSequentialJobId() ?: 0L
            maxSeq + 1L
        }
        val now = System.currentTimeMillis()
        val updated = job.copy(id = nextId, last_updated = now)
        val id = getJobDao().insertJob(updated)
        val insertedJob = updated.copy(id = if (id > 0) id else nextId)
        getCustomerDao()?.insertCustomer(
            CustomerEntity(
                name = job.customerName,
                contact = job.customerPhone,
                address = job.address,
                latitude = job.latitude,
                longitude = job.longitude,
                serviceRequired = job.serviceType,
                issueDescription = job.issueDescription,
                last_updated = now
            )
        )
        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            try {
                syncManager.pushJobDirectly(insertedJob)
            } catch (e: Exception) {
                android.util.Log.e("DispatchRepository", "Push job direct error: ${e.message}", e)
            }
            syncManager.scope.launch {
                syncManager.syncNow()
            }
        }
        return if (id > 0) id else nextId
    }

    suspend fun updateJob(job: CustomerJobEntity) {
        val updated = job.copy(last_updated = System.currentTimeMillis())
        getJobDao().updateJob(updated)
        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            try {
                syncManager.pushJobDirectly(updated)
            } catch (_: Exception) {}
            syncManager.scope.launch {
                syncManager.syncNow()
            }
        }
    }

    suspend fun unassignExpertFromJob(jobId: Long) {
        val now = System.currentTimeMillis()
        getJobDao().unassignExpertFromJob(jobId)
        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            syncManager.scope.launch {
                try {
                    val updates = mapOf<String, Any?>(
                        "status" to JobStatus.PENDING.name,
                        "assignedExpertId" to null,
                        "assignedExpertName" to null,
                        "assignedExpertPhone" to null,
                        "assigned_technician_id" to null,
                        "assigned_technician_name" to null,
                        "last_updated" to now
                    )
                    syncManager.getFirestore()?.collection(FirestoreSyncManager.JOBS_COLLECTION)
                        ?.document("job_$jobId")
                        ?.update(updates)
                        ?.await()
                } catch (_: Exception) {}
                syncManager.syncNow()
            }
        }
    }

    suspend fun updateJobStatus(jobId: Long, status: JobStatus) {
        val now = System.currentTimeMillis()
        getJobDao().updateJobStatus(jobId, status.name)
        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            syncManager.scope.launch {
                try {
                    syncManager.getFirestore()?.collection(FirestoreSyncManager.JOBS_COLLECTION)
                        ?.document("job_$jobId")
                        ?.update(mapOf("status" to status.name, "last_updated" to now))
                        ?.await()
                } catch (_: Exception) {}
                syncManager.syncNow()
            }
        }
    }

    suspend fun updateExpertNotified(jobId: Long, sent: Boolean) {
        val now = System.currentTimeMillis()
        getJobDao().updateExpertNotified(jobId, sent)
        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            syncManager.scope.launch {
                try {
                    syncManager.getFirestore()?.collection(FirestoreSyncManager.JOBS_COLLECTION)
                        ?.document("job_$jobId")
                        ?.update(mapOf("isExpertNotified" to sent, "last_updated" to now))
                        ?.await()
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun updateCustomerNotifiedOnAssign(jobId: Long, sent: Boolean) {
        val now = System.currentTimeMillis()
        getJobDao().updateCustomerNotifiedOnAssign(jobId, sent)
        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            syncManager.scope.launch {
                try {
                    syncManager.getFirestore()?.collection(FirestoreSyncManager.JOBS_COLLECTION)
                        ?.document("job_$jobId")
                        ?.update(mapOf("isCustomerNotifiedOnAssign" to sent, "last_updated" to now))
                        ?.await()
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun updateCustomerNotifiedOnCompletion(jobId: Long, sent: Boolean) {
        val now = System.currentTimeMillis()
        getJobDao().updateCustomerNotifiedOnCompletion(jobId, sent)
        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            syncManager.scope.launch {
                try {
                    syncManager.getFirestore()?.collection(FirestoreSyncManager.JOBS_COLLECTION)
                        ?.document("job_$jobId")
                        ?.update(mapOf("isCustomerNotifiedOnCompletion" to sent, "last_updated" to now))
                        ?.await()
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun updateMessageDismissedAt(jobId: Long, time: Long?) {
        getJobDao().updateMessageDismissedAt(jobId, time)
    }

    suspend fun completeOrCancelJobWithReview(
        jobId: Long,
        expertId: Long?,
        isCompleted: Boolean,
        rating: Float,
        feedback: String?,
        beforeUrl: String? = null,
        afterUrl: String? = null
    ) {
        val newStatus = if (isCompleted) JobStatus.COMPLETED.name else JobStatus.CANCELLED.name
        val now = System.currentTimeMillis()
        getJobDao().completeOrCancelJobWithReview(
            jobId = jobId,
            status = newStatus,
            rating = rating,
            feedback = feedback,
            completedAt = now,
            beforeUrl = beforeUrl,
            afterUrl = afterUrl
        )

        // Update expert's review metrics in database
        var updatedExpert: ExpertEntity? = null
        if (expertId != null && expertId > 0) {
            val expertDao = getExpertDao()
            val expert = expertDao.getExpertById(expertId)
            if (expert != null) {
                val newCount = expert.totalRatingsCount + 1
                val newSum = expert.ratingSum + rating
                val newAvg = (newSum / newCount)
                val roundedRating = Math.round(newAvg * 10f) / 10f

                val uExp = expert.copy(
                    rating = roundedRating,
                    ratingSum = newSum,
                    totalRatingsCount = newCount,
                    completedJobsCount = if (isCompleted) expert.completedJobsCount + 1 else expert.completedJobsCount,
                    cancelledJobsCount = if (!isCompleted) expert.cancelledJobsCount + 1 else expert.cancelledJobsCount,
                    last_updated = now
                )
                expertDao.updateExpert(uExp)
                updatedExpert = uExp
            }
        }

        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            syncManager.scope.launch {
                try {
                    val firestore = syncManager.getFirestore()
                    val jobUpdates = mapOf<String, Any?>(
                        "status" to newStatus,
                        "ratingGiven" to rating,
                        "reviewFeedback" to feedback,
                        "technicianBeforePhotoUrl" to beforeUrl,
                        "technicianAfterPhotoUrl" to afterUrl,
                        "completedAt" to now,
                        "last_updated" to now
                    )
                    firestore?.collection(FirestoreSyncManager.JOBS_COLLECTION)
                        ?.document("job_$jobId")
                        ?.update(jobUpdates)
                        ?.await()

                    if (updatedExpert != null) {
                        firestore?.collection(FirestoreSyncManager.EXPERTS_COLLECTION)
                            ?.document("expert_${updatedExpert.id}")
                            ?.set(expertToMap(updatedExpert), com.google.firebase.firestore.SetOptions.merge())
                            ?.await()
                    }
                } catch (_: Exception) {}
                syncManager.syncNow()
            }
        }
    }

    private fun jobToMap(job: CustomerJobEntity): Map<String, Any?> {
        return mapOf(
            "id" to job.id,
            "customerName" to job.customerName,
            "customerPhone" to job.customerPhone,
            "serviceType" to job.serviceType,
            "issueDescription" to job.issueDescription,
            "address" to job.address,
            "latitude" to job.latitude,
            "longitude" to job.longitude,
            "status" to job.status,
            "assignedExpertId" to job.assignedExpertId,
            "assignedExpertName" to job.assignedExpertName,
            "assignedExpertPhone" to job.assignedExpertPhone,
            "distanceKmAtDispatch" to job.distanceKmAtDispatch,
            "ratingGiven" to job.ratingGiven,
            "reviewFeedback" to job.reviewFeedback,
            "customerMediaUrls" to job.customerMediaUrls,
            "technicianBeforePhotoUrl" to job.technicianBeforePhotoUrl,
            "technicianAfterPhotoUrl" to job.technicianAfterPhotoUrl,
            "createdAt" to job.createdAt,
            "completedAt" to job.completedAt,
            "isExpertNotified" to job.isExpertNotified,
            "isCustomerNotifiedOnAssign" to job.isCustomerNotifiedOnAssign,
            "isCustomerNotifiedOnCompletion" to job.isCustomerNotifiedOnCompletion,
            "isDeleted" to job.isDeleted,
            "deletedAt" to job.deletedAt,
            "created_by_user_id" to job.created_by_user_id,
            "created_by_user_name" to job.created_by_user_name,
            "created_by_designation" to job.created_by_designation,
            "managed_by_user_id" to job.managed_by_user_id,
            "managed_by_user_name" to job.managed_by_user_name,
            "managed_by_designation" to job.managed_by_designation,
            "assigned_technician_id" to (job.assigned_technician_id ?: job.assignedExpertId),
            "assigned_technician_name" to (job.assigned_technician_name ?: job.assignedExpertName),
            "assigned_at_timestamp" to job.assigned_at_timestamp,
            "last_updated" to job.last_updated
        )
    }

    private fun expertToMap(exp: ExpertEntity): Map<String, Any?> {
        return mapOf(
            "id" to exp.id,
            "name" to exp.name,
            "phone" to exp.phone,
            "category" to exp.category,
            "address" to exp.address,
            "latitude" to exp.latitude,
            "longitude" to exp.longitude,
            "isAvailable" to exp.isAvailable,
            "rating" to exp.rating,
            "ratingSum" to exp.ratingSum,
            "totalRatingsCount" to exp.totalRatingsCount,
            "completedJobsCount" to exp.completedJobsCount,
            "cancelledJobsCount" to exp.cancelledJobsCount,
            "isWelcomeMessageSent" to exp.isWelcomeMessageSent,
            "isDeleted" to exp.isDeleted,
            "deletedAt" to exp.deletedAt,
            "added_by_user_id" to exp.added_by_user_id,
            "added_by_user_name" to exp.added_by_user_name,
            "added_by_designation" to exp.added_by_designation,
            "profilePicUrl" to exp.profilePicUrl,
            "profile_pic_url" to exp.profilePicUrl,
            "created_at_timestamp" to exp.created_at_timestamp,
            "createdAt" to exp.createdAt,
            "last_updated" to exp.last_updated
        )
    }

    suspend fun deleteJob(job: CustomerJobEntity) = getJobDao().deleteJob(job)

    suspend fun restoreDatabase(
        jobs: List<CustomerJobEntity>,
        experts: List<ExpertEntity>,
        categories: List<ExpertCategoryEntity>
    ) {
        if (jobs.isNotEmpty()) {
            getJobDao().insertJobs(jobs)
        }
        if (experts.isNotEmpty()) {
            getExpertDao().insertExperts(experts)
        }
        if (categories.isNotEmpty()) {
            val catDao = getCategoryDao()
            categories.forEach { catDao.insertCategory(it) }
        }
        context?.let { ctx ->
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                FirestoreSyncManager.getInstance(ctx).syncNow()
            }
        }
    }

    suspend fun findNearestExperts(
        customerLat: Double,
        customerLng: Double,
        requiredService: String? = null,
        onlyAvailable: Boolean = false
    ): List<RankedExpert> {
        val experts = if (onlyAvailable) {
            availableExperts.first()
        } else {
            allExperts.first()
        }

        return experts
            .filter { expert ->
                if (requiredService.isNullOrBlank() || requiredService == "All") {
                    true
                } else {
                    expert.category.contains(requiredService, ignoreCase = true) ||
                            expert.category.contains("All-Rounder", ignoreCase = true) ||
                            requiredService.contains(expert.category, ignoreCase = true)
                }
            }
            .map { expert ->
                val distance = LocationHelper.calculateDistanceKm(
                    lat1 = customerLat,
                    lon1 = customerLng,
                    lat2 = expert.latitude,
                    lon2 = expert.longitude
                )
                val travelMinutes = LocationHelper.estimateTravelTimeMinutes(distance)
                RankedExpert(
                    expert = expert,
                    distanceKm = distance,
                    travelTimeMinutes = travelMinutes
                )
            }
            .sortedBy { it.distanceKm }
    }

    suspend fun ensureDefaultCategoriesForCurrentUser() {
        val catDao = getCategoryDao()
        // Delete any default categories except Electrician and Plumber (keeps custom added ones)
        catDao.deleteOtherDefaultCategories()
        catDao.insertCategory(ExpertCategoryEntity(name = "Electrician", isDefault = true))
        catDao.insertCategory(ExpertCategoryEntity(name = "Plumber", isDefault = true))
        sanitizeOrderNumbers()
    }

    suspend fun sanitizeOrderNumbers() {
        try {
            val jobs = getJobDao().getAllJobsRaw()
            if (jobs.any { it.id >= 100000L }) {
                var seq = 1L
                for (job in jobs) {
                    if (job.id != seq) {
                        getJobDao().deleteJobById(job.id)
                        getJobDao().insertJob(job.copy(id = seq))
                    }
                    seq++
                }
            }
        } catch (_: Exception) {}
    }

    suspend fun seedSampleExpertsIfEmpty() {
        ensureDefaultCategoriesForCurrentUser()
    }

    suspend fun syncNow() {
        context?.let { ctx ->
            val syncManager = FirestoreSyncManager.getInstance(ctx)
            syncManager.startRealtimeSyncListeners()
            syncManager.syncNow()
        }
    }
}
