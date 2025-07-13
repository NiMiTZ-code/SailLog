package com.niemi.saillog.data

data class Sailboat(
    val id: String = "",
    val modelName: String = "",
    val imageUrl: String = "",
    val userId: String = "",
    val boatName: String = "My Sailboat", // Default
    val length: Double? = null,
    val year: Int? = null,
    val manufacturer: String? = null
)