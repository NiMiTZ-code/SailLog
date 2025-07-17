// data/model/Maintenance.kt
package com.niemi.saillog.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Maintenance(
    @DocumentId
    val id: String = "",
    val sailboatId: String = "",
    val userId: String = "",
    val category: MaintenanceCategory = MaintenanceCategory.OTHER,
    val notes: String = "",
    val timestamp: Timestamp? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

enum class MaintenanceCategory(val displayName: String) {
    ENGINE("Engine"),
    SAILS("Sails"),
    HULL("Hull"),
    ELECTRICAL("Electrical"),
    RIGGING("Rigging"),
    SAFETY("Safety Equipment"),
    COSMETICS("Cosmetics"),
    OTHER("Other");

    companion object {
        fun fromDisplayName(name: String): MaintenanceCategory {
            return values().find { it.displayName == name } ?: OTHER
        }
    }
}