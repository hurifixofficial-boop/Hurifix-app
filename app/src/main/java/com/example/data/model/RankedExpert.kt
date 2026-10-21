package com.example.data.model

data class RankedExpert(
    val expert: ExpertEntity,
    val distanceKm: Double,
    val travelTimeMinutes: Int
)
