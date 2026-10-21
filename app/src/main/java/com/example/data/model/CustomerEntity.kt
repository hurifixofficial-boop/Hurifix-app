package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val contact: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val serviceRequired: String = "",
    val issueDescription: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val last_updated: Long = System.currentTimeMillis(),
    val is_synced: Boolean = false
)
