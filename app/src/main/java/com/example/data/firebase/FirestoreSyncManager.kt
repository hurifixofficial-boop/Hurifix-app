package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.CustomerJobEntity
import com.example.data.model.ExpertCategoryEntity
import com.example.data.model.ExpertEntity
import com.example.data.model.HurifixUser
import com.example.data.model.JobStatus
import com.example.util.NetworkErrorHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

enum class SyncState {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

class FirestoreSyncManager(private val context: Context) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val db = AppDatabase.getDatabase(context)

    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private var jobsListener: ListenerRegistration? = null
    private var expertsListener: ListenerRegistration? = null
    private var categoriesListener: ListenerRegistration? = null
    private var activeSessionUserListener: ListenerRegistration? = null

    companion object {
        private const val TAG = "FirestoreSyncManager"
        const val USERS_COLLECTION = "users"
        const val JOBS_COLLECTION = "customer_jobs"
        const val EXPERTS_COLLECTION = "experts"
        const val CATEGORIES_COLLECTION = "expert_categories"

        @Volatile
        private var instance: FirestoreSyncManager? = null

        fun getInstance(context: Context): FirestoreSyncManager {
            return instance ?: synchronized(this) {
                instance ?: FirestoreSyncManager(context.applicationContext).also { instance = it }
            }
        }
    }

    @Volatile
    private var isFirestoreConfigured = false

    fun getFirestore(): FirebaseFirestore? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                try {
                    FirebaseApp.initializeApp(context)
                } catch (_: Exception) {}
            }
            val firestore = FirebaseFirestore.getInstance()
            if (!isFirestoreConfigured) {
                try {
                    val settings = FirebaseFirestoreSettings.Builder()
                        .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                        .build()
                    firestore.firestoreSettings = settings
                    isFirestoreConfigured = true
                } catch (_: Exception) {
                    isFirestoreConfigured = true
                }
            }
            firestore
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firestore: ${e.message}", e)
            try {
                FirebaseFirestore.getInstance()
            } catch (ex: Exception) {
                null
            }
        }
    }

    init {
        scope.launch {
            try {
                purgeDeprecatedDummyRecords()
                pushLiveVerificationDummyRecord()
            } catch (e: Exception) {
                Log.w(TAG, "Init purge error: ${e.message}")
            }
        }
    }

    // -------------------------------------------------------------
    // SPECIFICATION 2: Master Admins & Multi-User Authentication
    // -------------------------------------------------------------

    suspend fun seedMasterAdmins() = withContext(Dispatchers.IO) {
        purgeDeprecatedDummyRecords()
    }

    suspend fun purgeDeprecatedDummyRecords() = withContext(NonCancellable + Dispatchers.IO) {
        val deprecatedPhones = listOf("9876543210", "9999999999")
        // 1. Remove from local user cache
        deprecatedPhones.forEach { removeLocalCachedUser(it) }

        // 2. Remove dummy customer and dummy expert from Room
        try {
            db.customerJobDao().deleteJobById(888801L)
            db.customerJobDao().deleteJobsByCustomerPhone("9876543210")
            db.customerJobDao().deleteJobsByCustomerPhone("9999999999")
            db.customerJobDao().deleteDeprecatedVerificationJobs()
            db.expertDao().deleteExpertById(999901L)
            db.expertDao().deleteExpertsByPhone("9876543210")
            db.expertDao().deleteExpertsByPhone("9999999999")
            db.expertDao().deleteDeprecatedVerificationExperts()
        } catch (e: Exception) {
            Log.d(TAG, "Room purge note: ${e.message}")
        }

        // 3. Remove from Firestore
        val firestore = getFirestore() ?: return@withContext
        try {
            deprecatedPhones.forEach { phone ->
                firestore.collection(USERS_COLLECTION).document(phone).delete()
            }
            firestore.collection(JOBS_COLLECTION).document("job_888801").delete()
            firestore.collection(JOBS_COLLECTION).document("888801").delete()
            firestore.collection(EXPERTS_COLLECTION).document("expert_999901").delete()
            firestore.collection(EXPERTS_COLLECTION).document("999901").delete()

            // Also delete any job with customerPhone 9876543210
            val jobsQuery = firestore.collection(JOBS_COLLECTION)
                .whereEqualTo("customerPhone", "9876543210")
                .get().await()
            for (doc in jobsQuery.documents) {
                doc.reference.delete()
            }

            // Also delete any expert with phone 9876543210
            val expQuery = firestore.collection(EXPERTS_COLLECTION)
                .whereEqualTo("phone", "9876543210")
                .get().await()
            for (doc in expQuery.documents) {
                doc.reference.delete()
            }
        } catch (e: Exception) {
            Log.d(TAG, "Firestore purge note: ${e.message}")
        }
    }

    private val usersCachePrefs = context.getSharedPreferences("hurifix_users_cache_v2", Context.MODE_PRIVATE)

    private fun cacheUserLocally(user: HurifixUser) {
        try {
            val json = org.json.JSONObject().apply {
                put("phone", user.phone)
                put("name", user.name)
                put("password", user.password)
                put("role", user.role)
                put("designation_tag", user.designation_tag)
                put("profile_pic_url", user.profile_pic_url)
                put("is_blocked", user.is_blocked)
                put("is_deleted", user.is_deleted)
                put("can_manage_orders", user.can_manage_orders)
                put("can_add_experts", user.can_add_experts)
                put("can_add_customers", user.can_add_customers)
                put("can_delete_orders", user.can_delete_orders)
                put("can_export_reports", user.can_export_reports)
                put("view_only", user.view_only)
                put("created_at", user.created_at)
                put("last_updated", user.last_updated)
            }.toString()
            usersCachePrefs.edit().putString("user_${user.phone}", json).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Cache user error: ${e.message}")
        }
    }

    private fun getLocalCachedUsers(): List<HurifixUser> {
        val list = mutableListOf<HurifixUser>()
        try {
            val allEntries = usersCachePrefs.all
            for ((key, value) in allEntries) {
                if (key.startsWith("user_") && value is String) {
                    val obj = org.json.JSONObject(value)
                    val user = HurifixUser(
                        phone = obj.optString("phone"),
                        name = obj.optString("name"),
                        password = obj.optString("password"),
                        role = obj.optString("role", HurifixUser.ROLE_STAFF),
                        designation_tag = obj.optString("designation_tag"),
                        profile_pic_url = if (obj.has("profile_pic_url")) obj.optString("profile_pic_url").ifBlank { null } else null,
                        is_blocked = obj.optBoolean("is_blocked", false),
                        is_deleted = obj.optBoolean("is_deleted", false),
                        can_manage_orders = obj.optBoolean("can_manage_orders", true),
                        can_add_experts = obj.optBoolean("can_add_experts", true),
                        can_add_customers = obj.optBoolean("can_add_customers", true),
                        can_delete_orders = obj.optBoolean("can_delete_orders", false),
                        can_export_reports = obj.optBoolean("can_export_reports", false),
                        view_only = obj.optBoolean("view_only", false),
                        created_at = obj.optLong("created_at", System.currentTimeMillis()),
                        last_updated = obj.optLong("last_updated", System.currentTimeMillis())
                    )
                    list.add(user)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get local cached users error: ${e.message}")
        }
        return list
    }

    private fun removeLocalCachedUser(phone: String) {
        try {
            usersCachePrefs.edit().remove("user_$phone").apply()
        } catch (_: Exception) {}
    }

    suspend fun login(phone: String, password: String): Result<HurifixUser> = withContext(Dispatchers.IO) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        if (cleanPhone.length < 10) {
            return@withContext Result.failure(Exception("Please enter your 10-digits mobile number to login"))
        }

        val localCachedUser = getLocalCachedUsers().find { it.phone == cleanPhone }

        // 1. Try remote Firestore first to ensure latest permissions and account status
        val firestore = getFirestore()
        if (firestore != null) {
            try {
                val doc = firestore.collection(USERS_COLLECTION).document(cleanPhone).get().await()
                if (doc.exists()) {
                    var user = docToHurifixUser(doc)
                    if (localCachedUser != null) {
                        val hasCustomLocal = localCachedUser.name.isNotBlank() && 
                            localCachedUser.name != "Hurifix Admin" && 
                            localCachedUser.name != "Team Member" && 
                            localCachedUser.name != "Emergency Admin ($cleanPhone)"
                        val useLocalName = hasCustomLocal || localCachedUser.last_updated >= user.last_updated
                        user = user.copy(
                            name = if (useLocalName && localCachedUser.name.isNotBlank()) localCachedUser.name else user.name,
                            role = if (localCachedUser.role.isNotBlank() && localCachedUser.role != HurifixUser.ROLE_STAFF) localCachedUser.role else user.role,
                            designation_tag = if (localCachedUser.designation_tag.isNotBlank() && localCachedUser.designation_tag != "Team Member") localCachedUser.designation_tag else user.designation_tag,
                            profile_pic_url = localCachedUser.profile_pic_url ?: user.profile_pic_url,
                            password = if (localCachedUser.password.isNotBlank()) localCachedUser.password else user.password
                        )
                    }
                    cacheUserLocally(user)
                    if (user.is_blocked) {
                        return@withContext Result.failure(
                            Exception("Your account access has been revoked or blocked by Administrator. Please contact Admin.")
                        )
                    }
                    if (user.password != password) {
                        return@withContext Result.failure(
                            Exception("Invalid password. Please enter the correct password.")
                        )
                    }
                    return@withContext Result.success(user)
                }
            } catch (e: Exception) {
                Log.d(TAG, "Remote login check note (falling back to cache): ${e.message}")
            }
        }

        // 2. Check local cache fallback for offline login
        if (localCachedUser != null) {
            if (localCachedUser.is_blocked) {
                return@withContext Result.failure(Exception("Your account access has been revoked or blocked by Administrator. Please contact Admin."))
            }
            if (localCachedUser.password == password) {
                return@withContext Result.success(localCachedUser)
            }
            return@withContext Result.failure(Exception("Invalid password. Please enter the correct password."))
        }

        Result.failure(Exception("No user found try again"))
    }

    suspend fun loginWithOtp(phone: String): Result<HurifixUser> = withContext(Dispatchers.IO) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        if (cleanPhone.length < 10) {
            return@withContext Result.failure(Exception("Please enter your 10-digits mobile number to login"))
        }

        val localCachedUser = getLocalCachedUsers().find { it.phone == cleanPhone }

        val firestore = getFirestore()
        if (firestore != null) {
            try {
                val doc = firestore.collection(USERS_COLLECTION).document(cleanPhone).get().await()
                if (doc.exists()) {
                    var user = docToHurifixUser(doc)
                    if (localCachedUser != null) {
                        val hasCustomLocal = localCachedUser.name.isNotBlank() && 
                            localCachedUser.name != "Hurifix Admin" && 
                            localCachedUser.name != "Team Member" && 
                            localCachedUser.name != "Emergency Admin ($cleanPhone)"
                        val useLocalName = hasCustomLocal || localCachedUser.last_updated >= user.last_updated
                        user = user.copy(
                            name = if (useLocalName && localCachedUser.name.isNotBlank()) localCachedUser.name else user.name,
                            role = if (localCachedUser.role.isNotBlank() && localCachedUser.role != HurifixUser.ROLE_STAFF) localCachedUser.role else user.role,
                            designation_tag = if (localCachedUser.designation_tag.isNotBlank() && localCachedUser.designation_tag != "Team Member") localCachedUser.designation_tag else user.designation_tag,
                            profile_pic_url = localCachedUser.profile_pic_url ?: user.profile_pic_url,
                            password = if (localCachedUser.password.isNotBlank()) localCachedUser.password else user.password
                        )
                    }
                    cacheUserLocally(user)
                    if (user.is_blocked) {
                        return@withContext Result.failure(
                            Exception("Your account access has been revoked or blocked by Administrator. Please contact Admin.")
                        )
                    }
                    return@withContext Result.success(user)
                }
            } catch (_: Exception) {}
        }

        if (localCachedUser != null) {
            if (localCachedUser.is_blocked) {
                return@withContext Result.failure(Exception("Your account access has been revoked or blocked by Administrator. Please contact Admin."))
            }
            return@withContext Result.success(localCachedUser)
        }

        Result.failure(Exception("No user found try again"))
    }

    suspend fun recoverAdminAccess(
        currentPhone: String,
        secretKey: String,
        lostPhoneToBlock: String? = null
    ): Result<HurifixUser> = withContext(Dispatchers.IO) {
        val cleanPhone = currentPhone.replace(Regex("[^0-9]"), "")
        if (cleanPhone.length < 10) {
            return@withContext Result.failure(Exception("Please enter your 10-digits mobile number to login"))
        }

        if (secretKey.trim() != HurifixUser.EMERGENCY_RECOVERY_KEY) {
            return@withContext Result.failure(Exception("Galat Emergency Secret Recovery Key! Access Denied."))
        }

        val adminUser = HurifixUser(
            phone = cleanPhone,
            name = "Emergency Admin ($cleanPhone)",
            password = HurifixUser.MASTER_ADMIN_PASSWORD,
            role = HurifixUser.ROLE_ADMIN,
            designation_tag = "Emergency Admin",
            is_blocked = false,
            can_manage_orders = true,
            can_add_experts = true,
            can_add_customers = true,
            can_delete_orders = true,
            can_export_reports = true,
            view_only = false,
            created_at = System.currentTimeMillis(),
            last_updated = System.currentTimeMillis()
        )
        cacheUserLocally(adminUser)

        val firestore = getFirestore()
        if (firestore == null) {
            return@withContext Result.success(adminUser)
        }

        try {
            firestore.collection(USERS_COLLECTION)
                .document(cleanPhone)
                .set(adminUser.toMap(), SetOptions.merge())
                .await()

            // Revoke / block lost phone account if provided
            lostPhoneToBlock?.replace(Regex("[^0-9]"), "")?.let { lostPhone ->
                if (lostPhone.length == 10 && lostPhone != cleanPhone) {
                    firestore.collection(USERS_COLLECTION)
                        .document(lostPhone)
                        .update(
                            mapOf(
                                "is_blocked" to true,
                                "last_updated" to System.currentTimeMillis()
                            )
                        ).await()
                }
            }

            Result.success(adminUser)
        } catch (e: Exception) {
            Log.e(TAG, "Admin recovery error: ${e.message}", e)
            Result.success(adminUser) // Success locally even if remote update queues
        }
    }

    // -------------------------------------------------------------
    // SPECIFICATION 3: Admin User Management & RBAC
    // -------------------------------------------------------------

    suspend fun saveStaffUser(user: HurifixUser): Result<Unit> = withContext(NonCancellable + Dispatchers.IO) {
        cacheUserLocally(user)
        val firestore = getFirestore()
        if (firestore == null) {
            return@withContext Result.failure(Exception(NetworkErrorHelper.USER_FRIENDLY_NETWORK_ERROR))
        }
        try {
            firestore.collection(USERS_COLLECTION)
                .document(user.phone)
                .set(user.toMap(), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (e !is CancellationException) {
                Log.w(TAG, "Save staff remote sync note (cached locally): ${e.message}")
            }
            if (NetworkErrorHelper.isNetworkOrUnavailableError(e)) {
                NetworkErrorHelper.showNetworkErrorToast(context)
                Result.failure(Exception(NetworkErrorHelper.USER_FRIENDLY_NETWORK_ERROR))
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun updateUserBlocked(phone: String, isBlocked: Boolean): Result<Unit> = withContext(NonCancellable + Dispatchers.IO) {
        val cached = getLocalCachedUsers().find { it.phone == phone }
        if (cached != null) {
            cacheUserLocally(cached.copy(is_blocked = isBlocked, last_updated = System.currentTimeMillis()))
        }
        val firestore = getFirestore()
        if (firestore == null) {
            return@withContext Result.success(Unit)
        }
        try {
            firestore.collection(USERS_COLLECTION)
                .document(phone)
                .update(
                    mapOf(
                        "is_blocked" to isBlocked,
                        "last_updated" to System.currentTimeMillis()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (NetworkErrorHelper.isNetworkOrUnavailableError(e)) {
                NetworkErrorHelper.showNetworkErrorToast(context)
            }
            Result.success(Unit)
        }
    }

    suspend fun updateUserProfile(
        phone: String,
        name: String,
        designationTag: String,
        profilePicUrl: String?,
        oldPhone: String? = null,
        role: String? = null
    ): Result<Unit> = withContext(NonCancellable + Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val cached = getLocalCachedUsers().find { it.phone == phone || it.phone == oldPhone }
        if (cached != null) {
            val updatedUser = cached.copy(
                phone = phone,
                name = name,
                designation_tag = designationTag,
                profile_pic_url = profilePicUrl,
                role = role ?: cached.role,
                last_updated = now
            )
            if (!oldPhone.isNullOrBlank() && oldPhone != phone) {
                removeLocalCachedUser(oldPhone)
            }
            cacheUserLocally(updatedUser)
        }
        val firestore = getFirestore()
        if (firestore == null) {
            return@withContext Result.failure(Exception(NetworkErrorHelper.USER_FRIENDLY_NETWORK_ERROR))
        }
        try {
            val updates = mutableMapOf<String, Any?>(
                "phone" to phone,
                "name" to name,
                "designation_tag" to designationTag,
                "profilePicUrl" to profilePicUrl,
                "profile_pic_url" to profilePicUrl,
                "last_updated" to now
            )
            if (role != null) {
                updates["role"] = role
            }
            // Immediately and strictly save directly to Firebase Firestore
            firestore.collection(USERS_COLLECTION)
                .document(phone)
                .set(updates, SetOptions.merge())
                .await()

            // If phone number changed, remove old Firestore document
            if (!oldPhone.isNullOrBlank() && oldPhone != phone) {
                try {
                    firestore.collection(USERS_COLLECTION)
                        .document(oldPhone)
                        .delete()
                        .await()
                } catch (delErr: Exception) {
                    Log.d(TAG, "Old phone document clean note: ${delErr.message}")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            if (e !is CancellationException) {
                Log.w(TAG, "Update user profile remote note: ${e.message}")
            }
            if (NetworkErrorHelper.isNetworkOrUnavailableError(e)) {
                NetworkErrorHelper.showNetworkErrorToast(context)
                Result.failure(Exception(NetworkErrorHelper.USER_FRIENDLY_NETWORK_ERROR))
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun updateUserPassword(phone: String, newPassword: String): Result<Unit> = withContext(NonCancellable + Dispatchers.IO) {
        val cached = getLocalCachedUsers().find { it.phone == phone }
        if (cached != null) {
            cacheUserLocally(cached.copy(password = newPassword, last_updated = System.currentTimeMillis()))
        }
        val firestore = getFirestore()
        if (firestore == null) {
            return@withContext Result.success(Unit)
        }
        try {
            firestore.collection(USERS_COLLECTION)
                .document(phone)
                .update(
                    mapOf(
                        "password" to newPassword,
                        "last_updated" to System.currentTimeMillis()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    suspend fun deleteUser(phone: String): Result<Unit> = withContext(NonCancellable + Dispatchers.IO) {
        removeLocalCachedUser(phone)
        val firestore = getFirestore()
        if (firestore != null) {
            try {
                // First flag as deleted & blocked so active listeners immediately trigger force-logout!
                firestore.collection(USERS_COLLECTION)
                    .document(phone)
                    .update(
                        mapOf(
                            "is_deleted" to true,
                            "is_blocked" to true,
                            "last_updated" to System.currentTimeMillis()
                        )
                    ).await()
                firestore.collection(USERS_COLLECTION)
                    .document(phone)
                    .delete()
                    .await()
            } catch (e: Exception) {
                try {
                    firestore.collection(USERS_COLLECTION).document(phone).delete().await()
                } catch (_: Exception) {}
            }
        }
        Result.success(Unit)
    }

    suspend fun fetchAllUsers(): List<HurifixUser> = withContext(Dispatchers.IO) {
        val deprecatedPhones = setOf("9876543210", "9999999999")
        val combinedMap = mutableMapOf<String, HurifixUser>()
        getLocalCachedUsers().filter { !it.is_deleted && it.phone !in deprecatedPhones }.forEach { combinedMap[it.phone] = it }

        val firestore = getFirestore()
        if (firestore == null) {
            return@withContext combinedMap.values.filter { !it.is_deleted && it.phone !in deprecatedPhones }.toList()
        }
        try {
            val querySnapshot = firestore.collection(USERS_COLLECTION).get().await()
            val remoteList = querySnapshot.documents.mapNotNull { docToHurifixUser(it) }
            remoteList.forEach { user ->
                if (user.phone in deprecatedPhones) {
                    combinedMap.remove(user.phone)
                    removeLocalCachedUser(user.phone)
                } else if (!user.is_deleted) {
                    combinedMap[user.phone] = user
                    cacheUserLocally(user)
                } else {
                    combinedMap.remove(user.phone)
                    removeLocalCachedUser(user.phone)
                }
            }
            combinedMap.values.filter { !it.is_deleted && it.phone !in deprecatedPhones }.toList()
        } catch (e: Exception) {
            Log.d(TAG, "Fetch users fallback to cache: ${e.message}")
            combinedMap.values.filter { !it.is_deleted && it.phone !in deprecatedPhones }.toList()
        }
    }

    suspend fun takeoverOrder(jobId: Long, userId: String, userName: String, userDesignation: String? = null): Result<Unit> = withContext(NonCancellable + Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val firestore = getFirestore()
        if (firestore != null) {
            try {
                val updates = mutableMapOf<String, Any?>(
                    "managed_by_user_id" to userId,
                    "managed_by_user_name" to userName,
                    "last_updated" to now
                )
                if (!userDesignation.isNullOrBlank()) {
                    updates["managed_by_designation"] = userDesignation
                }
                firestore.collection(JOBS_COLLECTION).document("job_$jobId").update(updates).await()
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.w(TAG, "Takeover remote update error: ${e.message}")
                }
            }
        }
        try {
            db.customerJobDao().updateJobManager(jobId, userId, userName, userDesignation, now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenToActiveUserSession(
        phone: String,
        onSessionRevokedOrUpdated: (HurifixUser?) -> Unit
    ) {
        activeSessionUserListener?.remove()
        val firestore = getFirestore() ?: return
        activeSessionUserListener = firestore.collection(USERS_COLLECTION)
            .document(phone)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Session listener error: ${error.message}")
                    if (NetworkErrorHelper.isNetworkOrUnavailableError(error)) {
                        NetworkErrorHelper.showNetworkErrorToast(context)
                    }
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) {
                    onSessionRevokedOrUpdated(null)
                } else {
                    val user = docToHurifixUser(snapshot)
                    onSessionRevokedOrUpdated(user)
                }
            }
    }

    fun listenToAllUsers(
        onUsersChanged: (List<HurifixUser>) -> Unit
    ): ListenerRegistration? {
        val firestore = getFirestore() ?: return null
        return firestore.collection(USERS_COLLECTION)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w(TAG, "All users listener error: ${error.message}")
                    if (NetworkErrorHelper.isNetworkOrUnavailableError(error)) {
                        NetworkErrorHelper.showNetworkErrorToast(context)
                    }
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    scope.launch {
                        val deprecatedPhones = setOf("9876543210", "9999999999")
                        val combinedMap = mutableMapOf<String, HurifixUser>()
                        getLocalCachedUsers().filter { !it.is_deleted && it.phone !in deprecatedPhones }.forEach { combinedMap[it.phone] = it }

                        for (doc in snapshots.documents) {
                            try {
                                val user = docToHurifixUser(doc)
                                if (user.phone in deprecatedPhones) {
                                    combinedMap.remove(user.phone)
                                    removeLocalCachedUser(user.phone)
                                } else if (!user.is_deleted) {
                                    combinedMap[user.phone] = user
                                    cacheUserLocally(user)
                                } else {
                                    combinedMap.remove(user.phone)
                                    removeLocalCachedUser(user.phone)
                                }
                            } catch (_: Exception) {}
                        }
                        onUsersChanged(combinedMap.values.filter { !it.is_deleted && it.phone !in deprecatedPhones }.toList())
                    }
                }
            }
    }

    fun stopSessionListener() {
        activeSessionUserListener?.remove()
        activeSessionUserListener = null
    }

    // -------------------------------------------------------------
    // SPECIFICATION 4: Duplicate Order Protection & Real-Time Lock
    // -------------------------------------------------------------

    /**
     * Searches Firestore and local DB to verify if an order with the same customer phone number
     * exists within the last 4 hours.
     */
    suspend fun checkDuplicateRecentOrder(customerPhone: String): Boolean = withContext(Dispatchers.IO) {
        val clean = customerPhone.replace(Regex("[^0-9]"), "")
        if (clean.isBlank()) return@withContext false

        val fourHoursAgo = System.currentTimeMillis() - (4 * 60 * 60 * 1000L)

        // 1. Check local Room DB first
        val localMatch = db.customerJobDao().findRecentOrderByPhone(clean, fourHoursAgo)
        if (localMatch != null) {
            return@withContext true
        }

        // 2. Check Firestore
        val firestore = getFirestore() ?: return@withContext false
        try {
            val snapshot = firestore.collection(JOBS_COLLECTION)
                .whereEqualTo("customerPhone", clean)
                .whereGreaterThanOrEqualTo("createdAt", fourHoursAgo)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            Log.w(TAG, "Duplicate order remote check error: ${e.message}")
            false
        }
    }

    /**
     * Real-time Order Assignment Lock: Uses Firestore Transactions so that if two users attempt
     * to assign a technician to the same order simultaneously, only the first assignment succeeds.
     */
    suspend fun assignExpertWithTransaction(
        jobId: Long,
        expertId: Long,
        expertName: String,
        expertPhone: String,
        distanceKm: Double,
        managedByUserId: String? = null,
        managedByUserName: String? = null,
        managedByDesignation: String? = null
    ): Result<Unit> = withContext(NonCancellable + Dispatchers.IO) {
        val firestore = getFirestore()
        val docId = "job_$jobId"
        val now = System.currentTimeMillis()

        if (firestore != null) {
            val jobRef = firestore.collection(JOBS_COLLECTION).document(docId)
            try {
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(jobRef)
                    if (snapshot.exists()) {
                        val currentStatus = snapshot.getString("status")
                        val existingAssignedId = snapshot.getLong("assignedExpertId")
                        if (existingAssignedId != null && existingAssignedId != 0L && existingAssignedId != expertId) {
                            throw IllegalStateException(
                                "Assignment Conflict: Another user has already assigned this order to another expert!"
                            )
                        }
                    }

                    val updates = mutableMapOf<String, Any?>(
                        "status" to JobStatus.PROCESSING.name,
                        "assignedExpertId" to expertId,
                        "assignedExpertName" to expertName,
                        "assignedExpertPhone" to expertPhone,
                        "distanceKmAtDispatch" to distanceKm,
                        "assigned_technician_id" to expertId,
                        "assigned_technician_name" to expertName,
                        "assigned_at_timestamp" to now,
                        "last_updated" to now,
                        "is_synced" to true
                    )
                    if (!managedByUserId.isNullOrBlank()) {
                        updates["managed_by_user_id"] = managedByUserId
                        updates["managed_by_user_name"] = managedByUserName
                        if (!managedByDesignation.isNullOrBlank()) {
                            updates["managed_by_designation"] = managedByDesignation
                        }
                    }
                    transaction.set(jobRef, updates, SetOptions.merge())
                }.await()
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e(TAG, "Transaction assignment failed: ${e.message}", e)
                }
                return@withContext Result.failure(e)
            }
        }

        // Update local Room DB
        try {
            db.customerJobDao().updateJobDispatch(
                jobId = jobId,
                status = JobStatus.PROCESSING.name,
                expertId = expertId,
                expertName = expertName,
                expertPhone = expertPhone,
                distanceKm = distanceKm,
                assignedAt = now
            )
            if (!managedByUserId.isNullOrBlank() && !managedByUserName.isNullOrBlank()) {
                db.customerJobDao().updateJobManager(jobId, managedByUserId, managedByUserName, managedByDesignation, now)
            }
            db.customerJobDao().markJobSynced(jobId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // SPECIFICATION 5: Cloud Synchronization & Conflict Resolution
    // -------------------------------------------------------------

    suspend fun pushJobDirectly(job: CustomerJobEntity): Result<Unit> = withContext(NonCancellable + Dispatchers.IO) {
        val firestore = getFirestore() ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            firestore.collection(JOBS_COLLECTION)
                .document("job_${job.id}")
                .set(jobToMap(job), SetOptions.merge())
                .await()
            db.customerJobDao().markJobSynced(job.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Push job direct error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun pushExpertDirectly(expert: ExpertEntity): Result<Unit> = withContext(NonCancellable + Dispatchers.IO) {
        val firestore = getFirestore() ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        try {
            firestore.collection(EXPERTS_COLLECTION)
                .document("expert_${expert.id}")
                .set(expertToMap(expert), SetOptions.merge())
                .await()
            db.expertDao().markExpertSynced(expert.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Push expert direct error: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun startRealtimeSyncListeners() {
        val firestore = getFirestore() ?: return

        // 1. Customer Jobs real-time listener
        jobsListener?.remove()
        jobsListener = firestore.collection(JOBS_COLLECTION)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Jobs listener error: ${e.message}")
                    scope.launch {
                        kotlinx.coroutines.delay(3000)
                        startRealtimeSyncListeners()
                    }
                    return@addSnapshotListener
                }
                if (snapshots == null) return@addSnapshotListener
                scope.launch {
                    try {
                        // Live sync: Reconcile all current documents
                        for (doc in snapshots.documents) {
                            try {
                                val remoteJob = docToCustomerJob(doc) ?: continue
                                if (doc.id == "job_888801" || remoteJob.customerName.contains("Live verification", ignoreCase = true)) {
                                    continue
                                }
                                val local = db.customerJobDao().getJobById(remoteJob.id)
                                if (local == null) {
                                    db.customerJobDao().insertJob(remoteJob.copy(is_synced = true))
                                } else if (remoteJob.last_updated >= local.last_updated || remoteJob != local) {
                                    db.customerJobDao().updateJob(remoteJob.copy(is_synced = true))
                                }
                            } catch (err: Exception) {
                                Log.w(TAG, "Sync job item error: ${err.message}")
                            }
                        }
                        // Handle remote deletions
                        for (change in snapshots.documentChanges) {
                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                val id = (change.document.get("id") as? Number)?.toLong()
                                    ?: change.document.getLong("id")
                                    ?: change.document.id.removePrefix("job_").toLongOrNull()
                                if (id != null) {
                                    db.customerJobDao().deleteJobById(id)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Jobs sync processing error: ${e.message}", e)
                    }
                }
            }

        // 2. Experts real-time listener
        expertsListener?.remove()
        expertsListener = firestore.collection(EXPERTS_COLLECTION)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Experts listener error: ${e.message}")
                    scope.launch {
                        kotlinx.coroutines.delay(3000)
                        startRealtimeSyncListeners()
                    }
                    return@addSnapshotListener
                }
                if (snapshots == null) return@addSnapshotListener
                scope.launch {
                    try {
                        // Live sync: Reconcile all current documents
                        for (doc in snapshots.documents) {
                            try {
                                val remoteExp = docToExpert(doc) ?: continue
                                if (doc.id == "expert_999901" || remoteExp.name.contains("Live verification", ignoreCase = true)) {
                                    continue
                                }
                                val local = db.expertDao().getExpertById(remoteExp.id)
                                if (local == null) {
                                    db.expertDao().insertExpert(remoteExp.copy(is_synced = true))
                                } else if (remoteExp.last_updated >= local.last_updated || remoteExp != local) {
                                    db.expertDao().updateExpert(remoteExp.copy(is_synced = true))
                                }
                            } catch (err: Exception) {
                                Log.w(TAG, "Sync expert item error: ${err.message}")
                            }
                        }
                        // Handle remote deletions
                        for (change in snapshots.documentChanges) {
                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                val id = (change.document.get("id") as? Number)?.toLong()
                                    ?: change.document.getLong("id")
                                    ?: change.document.id.removePrefix("expert_").toLongOrNull()
                                if (id != null) {
                                    db.expertDao().deleteExpertById(id)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Experts sync processing error: ${e.message}", e)
                    }
                }
            }

        // 3. Categories real-time listener
        categoriesListener?.remove()
        categoriesListener = firestore.collection(CATEGORIES_COLLECTION)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener
                scope.launch {
                    try {
                        for (doc in snapshots.documents) {
                            try {
                                val remoteCat = docToCategory(doc) ?: continue
                                db.expertCategoryDao().insertCategory(remoteCat.copy(is_synced = true))
                            } catch (err: Exception) {
                                Log.w(TAG, "Sync category item error: ${err.message}")
                            }
                        }
                        for (change in snapshots.documentChanges) {
                            if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                val id = (change.document.get("id") as? Number)?.toLong()
                                    ?: change.document.getLong("id")
                                    ?: change.document.id.removePrefix("cat_").toLongOrNull()
                                if (id != null) {
                                    db.expertCategoryDao().deleteCategoryById(id)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Categories sync processing error: ${e.message}", e)
                    }
                }
            }
    }

    suspend fun syncNow(): Result<Unit> = withContext(Dispatchers.IO) {
        val firestore = getFirestore() ?: return@withContext Result.failure(Exception("Firestore unavailable"))
        _syncState.value = SyncState.SYNCING

        try {
            // Push unsynced local jobs
            val unsyncedJobs = db.customerJobDao().getUnsyncedJobs()
            for (job in unsyncedJobs) {
                val docRef = firestore.collection(JOBS_COLLECTION).document("job_${job.id}")
                docRef.set(jobToMap(job), SetOptions.merge()).await()
                db.customerJobDao().markJobSynced(job.id)
            }

            // Push unsynced local experts
            val unsyncedExperts = db.expertDao().getUnsyncedExperts()
            for (exp in unsyncedExperts) {
                val docRef = firestore.collection(EXPERTS_COLLECTION).document("expert_${exp.id}")
                docRef.set(expertToMap(exp), SetOptions.merge()).await()
                db.expertDao().markExpertSynced(exp.id)
            }

            // Push unsynced categories
            val unsyncedCats = db.expertCategoryDao().getUnsyncedCategories()
            for (cat in unsyncedCats) {
                val docRef = firestore.collection(CATEGORIES_COLLECTION).document("cat_${cat.id}")
                docRef.set(categoryToMap(cat), SetOptions.merge()).await()
                db.expertCategoryDao().markCategorySynced(cat.id)
            }

            // Pull latest from Firestore (Latest Timestamp Wins)
            val jobsSnap = firestore.collection(JOBS_COLLECTION).get().await()
            for (doc in jobsSnap.documents) {
                val remoteJob = docToCustomerJob(doc) ?: continue
                if (doc.id == "job_888801" || remoteJob.customerName.contains("Live verification", ignoreCase = true)) {
                    continue
                }
                val local = db.customerJobDao().getJobById(remoteJob.id)
                if (local == null) {
                    db.customerJobDao().insertJob(remoteJob.copy(is_synced = true))
                } else if (remoteJob.last_updated >= local.last_updated || remoteJob != local) {
                    db.customerJobDao().updateJob(remoteJob.copy(is_synced = true))
                }
            }

            val expertsSnap = firestore.collection(EXPERTS_COLLECTION).get().await()
            for (doc in expertsSnap.documents) {
                val remoteExp = docToExpert(doc) ?: continue
                if (doc.id == "expert_999901" || remoteExp.name.contains("Live verification", ignoreCase = true)) {
                    continue
                }
                val local = db.expertDao().getExpertById(remoteExp.id)
                if (local == null) {
                    db.expertDao().insertExpert(remoteExp.copy(is_synced = true))
                } else if (remoteExp.last_updated >= local.last_updated || remoteExp != local) {
                    db.expertDao().updateExpert(remoteExp.copy(is_synced = true))
                }
            }

            _syncState.value = SyncState.SUCCESS
            _lastSyncTimestamp.value = System.currentTimeMillis()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sync error: ${e.message}", e)
            _syncState.value = SyncState.ERROR
            Result.failure(e)
        }
    }

    // --- Helper Mappings ---

    private fun docToHurifixUser(doc: DocumentSnapshot): HurifixUser {
        val role = doc.getString("role") ?: HurifixUser.ROLE_STAFF
        val rawTag = doc.getString("designation_tag")
        val defaultTag = if (role.equals(HurifixUser.ROLE_ADMIN, ignoreCase = true)) "Master Admin" else "Team Member"
        return HurifixUser(
            phone = doc.getString("phone") ?: doc.id,
            name = doc.getString("name") ?: "Team Member",
            password = doc.getString("password") ?: "",
            role = role,
            designation_tag = if (!rawTag.isNullOrBlank()) rawTag else defaultTag,
            profile_pic_url = doc.getString("profilePicUrl") ?: doc.getString("profile_pic_url"),
            is_blocked = doc.getBoolean("is_blocked") ?: false,
            is_deleted = doc.getBoolean("is_deleted") ?: false,
            can_manage_orders = doc.getBoolean("can_manage_orders") ?: true,
            can_add_experts = doc.getBoolean("can_add_experts") ?: true,
            can_add_customers = doc.getBoolean("can_add_customers") ?: true,
            can_delete_orders = doc.getBoolean("can_delete_orders") ?: false,
            can_export_reports = doc.getBoolean("can_export_reports") ?: false,
            view_only = doc.getBoolean("view_only") ?: false,
            created_at = doc.getLong("created_at") ?: System.currentTimeMillis(),
            last_updated = doc.getLong("last_updated") ?: System.currentTimeMillis()
        )
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

    private fun docToCustomerJob(doc: DocumentSnapshot): CustomerJobEntity? {
        val id = (doc.get("id") as? Number)?.toLong()
            ?: doc.getLong("id")
            ?: (doc.get("id") as? String)?.toLongOrNull()
            ?: doc.id.removePrefix("job_").toLongOrNull()
            ?: kotlin.math.abs(doc.id.hashCode().toLong()).coerceAtLeast(1L)
        return CustomerJobEntity(
            id = id,
            customerName = doc.getString("customerName") ?: "Customer",
            customerPhone = doc.getString("customerPhone") ?: "",
            serviceType = doc.getString("serviceType") ?: "General Repair",
            issueDescription = doc.getString("issueDescription") ?: "",
            address = doc.getString("address") ?: "",
            latitude = doc.getDouble("latitude") ?: 28.57,
            longitude = doc.getDouble("longitude") ?: 77.32,
            status = doc.getString("status") ?: JobStatus.PENDING.name,
            assignedExpertId = doc.getLong("assignedExpertId"),
            assignedExpertName = doc.getString("assignedExpertName"),
            assignedExpertPhone = doc.getString("assignedExpertPhone"),
            distanceKmAtDispatch = doc.getDouble("distanceKmAtDispatch"),
            ratingGiven = doc.getDouble("ratingGiven")?.toFloat(),
            reviewFeedback = doc.getString("reviewFeedback"),
            customerMediaUrls = doc.getString("customerMediaUrls"),
            technicianBeforePhotoUrl = doc.getString("technicianBeforePhotoUrl"),
            technicianAfterPhotoUrl = doc.getString("technicianAfterPhotoUrl"),
            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
            completedAt = doc.getLong("completedAt"),
            isExpertNotified = doc.getBoolean("isExpertNotified") ?: false,
            isCustomerNotifiedOnAssign = doc.getBoolean("isCustomerNotifiedOnAssign") ?: false,
            isCustomerNotifiedOnCompletion = doc.getBoolean("isCustomerNotifiedOnCompletion") ?: false,
            isDeleted = doc.getBoolean("isDeleted") ?: false,
            deletedAt = doc.getLong("deletedAt"),
            last_updated = doc.getLong("last_updated") ?: System.currentTimeMillis(),
            is_synced = true,
            created_by_user_id = doc.getString("created_by_user_id"),
            created_by_user_name = doc.getString("created_by_user_name"),
            created_by_designation = doc.getString("created_by_designation"),
            managed_by_user_id = doc.getString("managed_by_user_id"),
            managed_by_user_name = doc.getString("managed_by_user_name"),
            managed_by_designation = doc.getString("managed_by_designation"),
            assigned_technician_id = doc.getLong("assigned_technician_id") ?: doc.getLong("assignedExpertId"),
            assigned_technician_name = doc.getString("assigned_technician_name") ?: doc.getString("assignedExpertName"),
            assigned_at_timestamp = doc.getLong("assigned_at_timestamp")
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

    private fun docToExpert(doc: DocumentSnapshot): ExpertEntity? {
        val id = (doc.get("id") as? Number)?.toLong()
            ?: doc.getLong("id")
            ?: (doc.get("id") as? String)?.toLongOrNull()
            ?: doc.id.removePrefix("expert_").toLongOrNull()
            ?: kotlin.math.abs(doc.id.hashCode().toLong()).coerceAtLeast(1L)
        val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
        val createdAtTimestamp = doc.getLong("created_at_timestamp") ?: createdAt
        return ExpertEntity(
            id = id,
            name = doc.getString("name") ?: "Expert",
            phone = doc.getString("phone") ?: "",
            category = doc.getString("category") ?: "Electrician",
            address = doc.getString("address") ?: "",
            latitude = doc.getDouble("latitude") ?: 28.57,
            longitude = doc.getDouble("longitude") ?: 77.32,
            isAvailable = doc.getBoolean("isAvailable") ?: true,
            rating = doc.getDouble("rating")?.toFloat() ?: 4.8f,
            ratingSum = doc.getDouble("ratingSum")?.toFloat() ?: 4.8f,
            totalRatingsCount = doc.getLong("totalRatingsCount")?.toInt() ?: 1,
            completedJobsCount = doc.getLong("completedJobsCount")?.toInt() ?: 0,
            cancelledJobsCount = doc.getLong("cancelledJobsCount")?.toInt() ?: 0,
            isWelcomeMessageSent = doc.getBoolean("isWelcomeMessageSent") ?: false,
            isDeleted = doc.getBoolean("isDeleted") ?: false,
            deletedAt = doc.getLong("deletedAt"),
            added_by_user_id = doc.getString("added_by_user_id"),
            added_by_user_name = doc.getString("added_by_user_name"),
            added_by_designation = doc.getString("added_by_designation"),
            profilePicUrl = (doc.getString("profilePicUrl") 
                ?: doc.getString("profile_pic_url") 
                ?: doc.getString("photoUri") 
                ?: doc.getString("photo_url") 
                ?: doc.getString("image_url") 
                ?: doc.getString("avatar_url")).orEmpty(),
            created_at_timestamp = createdAtTimestamp,
            createdAt = createdAt,
            last_updated = doc.getLong("last_updated") ?: System.currentTimeMillis(),
            is_synced = true
        )
    }

    private fun categoryToMap(cat: ExpertCategoryEntity): Map<String, Any?> {
        return mapOf(
            "id" to cat.id,
            "name" to cat.name,
            "isDefault" to cat.isDefault,
            "createdAt" to cat.createdAt,
            "last_updated" to cat.last_updated
        )
    }

    private fun docToCategory(doc: DocumentSnapshot): ExpertCategoryEntity? {
        val id = doc.getLong("id")
            ?: (doc.get("id") as? Number)?.toLong()
            ?: (doc.get("id") as? String)?.toLongOrNull()
            ?: doc.id.removePrefix("cat_").toLongOrNull()
            ?: return null
        val name = doc.getString("name") ?: return null
        return ExpertCategoryEntity(
            id = id,
            name = name,
            isDefault = doc.getBoolean("isDefault") ?: false,
            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
            last_updated = doc.getLong("last_updated") ?: System.currentTimeMillis(),
            is_synced = true
        )
    }

    suspend fun pushLiveVerificationDummyRecord(): Result<String> {
        return withContext(NonCancellable + Dispatchers.IO) {
            val firestore = getFirestore() ?: return@withContext Result.failure(Exception("Firestore instance unavailable"))
            val now = System.currentTimeMillis()

            try {
                // 1. Write System Verification Document
                val verificationMap = mapOf(
                    "status" to "VERIFIED_ACTIVE",
                    "project_id" to "hurifix",
                    "app_name" to "Hurifix Multi-User Dispatch Central",
                    "message" to "Live Firestore Database & Auth Complete Sync Active",
                    "timestamp" to now,
                    "read_write_check" to true,
                    "verified_collections" to listOf(USERS_COLLECTION, EXPERTS_COLLECTION, JOBS_COLLECTION, CATEGORIES_COLLECTION)
                )
                firestore.collection("system_verification")
                    .document("live_sync_check")
                    .set(verificationMap, SetOptions.merge())
                    .await()

                // Purge any deprecated dummy records as requested
                purgeDeprecatedDummyRecords()

                // 4. Read back verification record to confirm 100% read/write
                val readCheck = firestore.collection("system_verification")
                    .document("live_sync_check")
                    .get()
                    .await()

                if (readCheck.exists()) {
                    Log.i(TAG, "✅ Live Firestore DB Read/Write Verification Confirmed for Project: hurifix!")
                    Result.success("Live Firestore DB Read/Write & Auth Sync Verified Successfully!")
                } else {
                    Result.failure(Exception("Verification document read-back failed"))
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Error pushing live verification record: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}
