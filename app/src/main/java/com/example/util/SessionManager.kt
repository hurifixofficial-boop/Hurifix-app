package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.firebase.FirestoreSyncManager
import com.example.data.model.HurifixUser
import kotlinx.coroutines.launch

class SessionManager(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("hurifix_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_PASSWORD = "user_password"
        private const val KEY_USER_PHOTO_URI = "user_photo_uri"
        private const val KEY_USER_DESIGNATION_TAG = "user_designation_tag"
        private const val KEY_DARK_MODE = "dark_mode_enabled"

        // RBAC Permission Keys
        private const val KEY_CAN_MANAGE_ORDERS = "can_manage_orders"
        private const val KEY_CAN_ADD_EXPERTS = "can_add_experts"
        private const val KEY_CAN_ADD_CUSTOMERS = "can_add_customers"
        private const val KEY_CAN_DELETE_ORDERS = "can_delete_orders"
        private const val KEY_CAN_EXPORT_REPORTS = "can_export_reports"
        private const val KEY_VIEW_ONLY = "view_only"
        private const val KEY_IS_BLOCKED = "is_blocked"

        // Auto Backup Settings
        private const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
        private const val KEY_AUTO_BACKUP_FREQUENCY = "auto_backup_frequency"
        private const val KEY_LAST_BACKUP_TIMESTAMP = "last_backup_timestamp"
        private const val KEY_LAST_BACKUP_STATUS = "last_backup_status"
    }

    // --- Auto-Backup Preferences ---

    fun isAutoBackupEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_BACKUP_ENABLED, true)

    fun setAutoBackupEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_BACKUP_ENABLED, enabled).apply()
    }

    fun getAutoBackupFrequency(): String = prefs.getString(KEY_AUTO_BACKUP_FREQUENCY, "DAILY") ?: "DAILY"

    fun setAutoBackupFrequency(frequency: String) {
        prefs.edit().putString(KEY_AUTO_BACKUP_FREQUENCY, frequency).apply()
    }

    fun getLastBackupTimestamp(): Long = prefs.getLong(KEY_LAST_BACKUP_TIMESTAMP, 0L)

    fun setLastBackupTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_BACKUP_TIMESTAMP, timestamp).apply()
    }

    fun getLastBackupStatus(): String = prefs.getString(KEY_LAST_BACKUP_STATUS, "No backup generated yet") ?: "No backup generated yet"

    fun setLastBackupStatus(status: String) {
        prefs.edit().putString(KEY_LAST_BACKUP_STATUS, status).apply()
    }

    fun isDarkModeEnabled(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)

    fun setDarkModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "Team Member") ?: "Team Member"

    fun getUserPhone(): String = prefs.getString(KEY_USER_PHONE, "") ?: ""

    fun getUserRole(): String = prefs.getString(KEY_USER_ROLE, HurifixUser.ROLE_STAFF) ?: HurifixUser.ROLE_STAFF

    fun getUserDesignationTag(): String {
        val tag = prefs.getString(KEY_USER_DESIGNATION_TAG, "") ?: ""
        if (tag.isNotBlank()) return tag
        return if (isAdmin()) "Master Admin" else "Team Member"
    }

    fun setUserDesignationTag(tag: String) {
        prefs.edit().putString(KEY_USER_DESIGNATION_TAG, tag.trim()).apply()
    }

    fun getUserPassword(): String = prefs.getString(KEY_USER_PASSWORD, "") ?: ""

    fun getUserPhotoUri(): String? {
        val phone = getUserPhone()
        return if (phone.isNotBlank()) {
            prefs.getString("user_photo_$phone", null) ?: prefs.getString(KEY_USER_PHOTO_URI, null)
        } else {
            prefs.getString(KEY_USER_PHOTO_URI, null)
        }
    }

    // --- Role-Based Access Control (RBAC) Checks ---

    fun isAdmin(): Boolean {
        val role = getUserRole()
        return role.equals(HurifixUser.ROLE_ADMIN, ignoreCase = true)
    }

    fun canManageOrders(): Boolean = !isViewOnly() && (isAdmin() || prefs.getBoolean(KEY_CAN_MANAGE_ORDERS, false))

    fun canAddExperts(): Boolean = !isViewOnly() && (isAdmin() || prefs.getBoolean(KEY_CAN_ADD_EXPERTS, true))

    fun canAddCustomers(): Boolean = !isViewOnly() && (isAdmin() || prefs.getBoolean(KEY_CAN_ADD_CUSTOMERS, true))

    fun canDeleteOrders(): Boolean = !isViewOnly() && (isAdmin() || prefs.getBoolean(KEY_CAN_DELETE_ORDERS, false))

    fun canExportReports(): Boolean = !isViewOnly() && (isAdmin() || prefs.getBoolean(KEY_CAN_EXPORT_REPORTS, false))

    fun isViewOnly(): Boolean = !isAdmin() && prefs.getBoolean(KEY_VIEW_ONLY, false)

    fun isBlocked(): Boolean = prefs.getBoolean(KEY_IS_BLOCKED, false)

    // --- Authentication Actions ---

    suspend fun login(phone: String, password: String): Result<String> {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val syncManager = FirestoreSyncManager.getInstance(context)
        val result = syncManager.login(cleanPhone, password)

        return result.map { user ->
            saveUserSession(user)
            user.name
        }
    }

    suspend fun loginWithOtp(phone: String): Result<String> {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val syncManager = FirestoreSyncManager.getInstance(context)
        val result = syncManager.loginWithOtp(cleanPhone)

        return result.map { user ->
            saveUserSession(user)
            user.name
        }
    }

    suspend fun recoverAdminAccess(
        phone: String,
        secretKey: String,
        lostPhoneToBlock: String? = null
    ): Result<String> {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val syncManager = FirestoreSyncManager.getInstance(context)
        val result = syncManager.recoverAdminAccess(cleanPhone, secretKey, lostPhoneToBlock)

        return result.map { adminUser ->
            saveUserSession(adminUser)
            adminUser.name
        }
    }

    fun saveUserSession(user: HurifixUser) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_PHONE, user.phone)
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_ROLE, user.role)
            putString(KEY_USER_DESIGNATION_TAG, user.displayDesignation)
            putString(KEY_USER_PASSWORD, user.password)
            if (!user.profile_pic_url.isNullOrBlank()) {
                putString(KEY_USER_PHOTO_URI, user.profile_pic_url)
                putString("user_photo_${user.phone}", user.profile_pic_url)
            }
            putBoolean(KEY_IS_BLOCKED, user.is_blocked)
            putBoolean(KEY_CAN_MANAGE_ORDERS, user.can_manage_orders)
            putBoolean(KEY_CAN_ADD_EXPERTS, user.can_add_experts)
            putBoolean(KEY_CAN_ADD_CUSTOMERS, user.can_add_customers)
            putBoolean(KEY_CAN_DELETE_ORDERS, user.can_delete_orders)
            putBoolean(KEY_CAN_EXPORT_REPORTS, user.can_export_reports)
            putBoolean(KEY_VIEW_ONLY, user.view_only)
            apply()
        }
    }

    fun updateUserProfile(name: String, phone: String, designationTag: String = "Team Member", photoUri: String? = null) {
        val oldPhone = getUserPhone()
        val cleanPhone = phone.replace(Regex("[^0-9]"), "").ifBlank { oldPhone }
        prefs.edit().apply {
            putString(KEY_USER_NAME, name.trim())
            putString(KEY_USER_PHONE, cleanPhone)
            putString(KEY_USER_DESIGNATION_TAG, designationTag.trim())
            putString(KEY_USER_PHOTO_URI, photoUri)
            if (cleanPhone.isNotBlank()) {
                putString("user_name_$cleanPhone", name.trim())
                putString("user_designation_$cleanPhone", designationTag.trim())
                putString("user_photo_$cleanPhone", photoUri)
            }
            apply()
        }
        val syncManager = FirestoreSyncManager.getInstance(context)
        val role = getUserRole()
        syncManager.scope.launch {
            syncManager.updateUserProfile(
                phone = cleanPhone,
                name = name.trim(),
                designationTag = designationTag.trim(),
                profilePicUrl = photoUri,
                role = role,
                oldPhone = if (oldPhone.isNotBlank() && oldPhone != cleanPhone) oldPhone else null
            )
        }
    }

    fun updateAdminProfile(name: String, phone: String, designationTag: String = "Co-Founder & Operations", photoUri: String? = null) {
        val oldPhone = getUserPhone()
        val cleanPhone = phone.replace(Regex("[^0-9]"), "").ifBlank { oldPhone }
        prefs.edit().apply {
            putString(KEY_USER_NAME, name.trim())
            putString(KEY_USER_PHONE, cleanPhone)
            putString(KEY_USER_ROLE, HurifixUser.ROLE_ADMIN)
            putString(KEY_USER_DESIGNATION_TAG, designationTag.trim())
            putString(KEY_USER_PHOTO_URI, photoUri)
            if (cleanPhone.isNotBlank()) {
                putString("user_name_$cleanPhone", name.trim())
                putString("user_designation_$cleanPhone", designationTag.trim())
                putString("user_photo_$cleanPhone", photoUri)
            }
            apply()
        }
        val syncManager = FirestoreSyncManager.getInstance(context)
        syncManager.scope.launch {
            syncManager.updateUserProfile(
                phone = cleanPhone,
                name = name.trim(),
                designationTag = designationTag.trim(),
                profilePicUrl = photoUri,
                role = HurifixUser.ROLE_ADMIN,
                oldPhone = if (oldPhone.isNotBlank() && oldPhone != cleanPhone) oldPhone else null
            )
        }
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> {
        val phone = getUserPhone()
        if (phone.isBlank()) return Result.failure(Exception("User not logged in"))
        val syncManager = FirestoreSyncManager.getInstance(context)
        val res = syncManager.updateUserPassword(phone, newPassword)
        if (res.isSuccess) {
            prefs.edit().putString(KEY_USER_PASSWORD, newPassword).apply()
        }
        return res
    }

    suspend fun deleteCurrentUserAccount(): Result<Unit> {
        val phone = getUserPhone()
        if (phone.isBlank()) return Result.failure(Exception("No active account to delete"))
        val syncManager = FirestoreSyncManager.getInstance(context)
        val res = syncManager.deleteUser(phone)
        logout()
        return res
    }

    fun logout() {
        FirestoreSyncManager.getInstance(context).stopSessionListener()
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            putString(KEY_USER_PHONE, "")
            putString(KEY_USER_NAME, "")
            putString(KEY_USER_PASSWORD, "")
            putString(KEY_USER_ROLE, HurifixUser.ROLE_STAFF)
            putBoolean(KEY_IS_BLOCKED, false)
            putBoolean(KEY_CAN_MANAGE_ORDERS, true)
            putBoolean(KEY_CAN_ADD_EXPERTS, true)
            putBoolean(KEY_CAN_ADD_CUSTOMERS, true)
            putBoolean(KEY_CAN_DELETE_ORDERS, false)
            putBoolean(KEY_CAN_EXPORT_REPORTS, false)
            putBoolean(KEY_VIEW_ONLY, false)
            remove(KEY_USER_PHOTO_URI)
            apply()
        }
    }
}
