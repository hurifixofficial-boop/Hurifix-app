package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class JobStatus(val label: String, val hindiLabel: String) {
    PENDING("Pending", "लंबित"),
    PROCESSING("Processing", "प्रक्रिया में"),
    COMPLETED("Completed", "पूर्ण"),
    CANCELLED("Cancelled", "रद्द")
}

@Serializable
@Entity(tableName = "customer_jobs")
data class CustomerJobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serviceType: String,
    val status: String = JobStatus.PENDING.name,
    val customerName: String,
    val customerPhone: String,
    val issueDescription: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val assignedExpertId: Long? = null,
    val assignedExpertName: String? = null,
    val assignedExpertPhone: String? = null,
    val distanceKmAtDispatch: Double? = null,
    val ratingGiven: Float? = null,
    val reviewFeedback: String? = null,
    val customerMediaUrls: String? = null,
    val technicianBeforePhotoUrl: String? = null,
    val technicianAfterPhotoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val isExpertNotified: Boolean = false,
    val isCustomerNotifiedOnAssign: Boolean = false,
    val isCustomerNotifiedOnCompletion: Boolean = false,
    val assignMessageLaterDismissedAt: Long? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val last_updated: Long = System.currentTimeMillis(),
    val is_synced: Boolean = false,
    val created_by_user_id: String? = null,
    val created_by_user_name: String? = null,
    val created_by_designation: String? = null,
    val managed_by_user_id: String? = null,
    val managed_by_user_name: String? = null,
    val managed_by_designation: String? = null,
    val assigned_technician_id: Long? = null,
    val assigned_technician_name: String? = null,
    val assigned_at_timestamp: Long? = null
) {
    val orderNumberFormatted: String
        get() = if (id in 1..999999) {
            String.format(java.util.Locale.US, "%03d", id)
        } else {
            String.format(java.util.Locale.US, "%03d", ((id % 1000) + 1).coerceIn(1, 999))
        }

    val orderDisplayTag: String
        get() = "Order #$orderNumberFormatted"
}
