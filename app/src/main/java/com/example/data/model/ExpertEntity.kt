package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "experts")
data class ExpertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val phone: String = "",
    val category: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isAvailable: Boolean = true,
    val rating: Float = 5.0f,
    val ratingSum: Float = 5.0f,
    val totalRatingsCount: Int = 1,
    val completedJobsCount: Int = 0,
    val cancelledJobsCount: Int = 0,
    val isWelcomeMessageSent: Boolean = false,
    val profilePicUrl: String = "",
    val added_by_user_id: String? = null,
    val added_by_user_name: String? = null,
    val added_by_designation: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val created_at_timestamp: Long = System.currentTimeMillis(),
    val last_updated: Long = System.currentTimeMillis(),
    val is_synced: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)
