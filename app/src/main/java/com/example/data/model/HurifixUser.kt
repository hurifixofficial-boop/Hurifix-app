package com.example.data.model

data class HurifixUser(
    val phone: String = "",
    val name: String = "",
    val password: String = "",
    val role: String = ROLE_STAFF,
    val designation_tag: String = "",
    val profile_pic_url: String? = null,
    val is_blocked: Boolean = false,
    val can_manage_orders: Boolean = false,
    val can_add_experts: Boolean = true,
    val can_add_customers: Boolean = true,
    val can_delete_orders: Boolean = false,
    val can_export_reports: Boolean = false,
    val view_only: Boolean = false,
    val is_deleted: Boolean = false,
    val created_at: Long = System.currentTimeMillis(),
    val last_updated: Long = System.currentTimeMillis()
) {
    val isPrimaryAdmin: Boolean
        get() = false

    val isAdmin: Boolean
        get() = role.equals(ROLE_ADMIN, ignoreCase = true)

    val displayDesignation: String
        get() {
            if (designation_tag.isNotBlank()) return designation_tag.trim()
            return if (isAdmin) "Master Admin" else "Team Member"
        }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "phone" to phone,
            "name" to name,
            "password" to password,
            "role" to role,
            "designation_tag" to displayDesignation,
            "profile_pic_url" to profile_pic_url,
            "is_blocked" to is_blocked,
            "is_deleted" to is_deleted,
            "can_manage_orders" to can_manage_orders,
            "can_add_experts" to can_add_experts,
            "can_add_customers" to can_add_customers,
            "can_delete_orders" to can_delete_orders,
            "can_export_reports" to can_export_reports,
            "view_only" to view_only,
            "created_at" to created_at,
            "last_updated" to last_updated
        )
    }

    companion object {
        const val ROLE_ADMIN = "ADMIN"
        const val ROLE_STAFF = "STAFF"
        const val PRIMARY_ADMIN_PHONE = ""
        const val SECONDARY_ADMIN_PHONE = ""
        const val MASTER_ADMIN_PASSWORD = "AdminPassword123"
        const val EMERGENCY_RECOVERY_KEY = "Hurifixkaran3455@##"

        val DEFAULT_DESIGNATION_PRESETS = listOf(
            "Co-Founder & Admin",
            "Operations Manager",
            "Dispatch Coordinator",
            "Field Executive"
        )

        fun createMasterAdmin(phone: String, name: String = "Hurifix Admin"): HurifixUser {
            return HurifixUser(
                phone = phone,
                name = name,
                password = MASTER_ADMIN_PASSWORD,
                role = ROLE_ADMIN,
                designation_tag = "Co-Founder & Admin",
                is_blocked = false,
                can_manage_orders = true,
                can_add_experts = true,
                can_add_customers = true,
                can_delete_orders = true,
                can_export_reports = true,
                view_only = false,
                is_deleted = false
            )
        }
    }
}
