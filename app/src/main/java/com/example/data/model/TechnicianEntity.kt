package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "technicians")
data class TechnicianEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val contact: String = "",
    val category: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isAvailable: Boolean = true,
    val rating: Float = 5.0f,
    val completedJobsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val last_updated: Long = System.currentTimeMillis(),
    val is_synced: Boolean = false
)
