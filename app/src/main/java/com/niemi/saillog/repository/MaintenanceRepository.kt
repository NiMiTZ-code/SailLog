package com.niemi.saillog.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.niemi.saillog.data.Maintenance
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MaintenanceRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val maintenanceCollection = firestore.collection("maintenance")

    suspend fun addMaintenance(maintenance: Maintenance): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val docRef = maintenanceCollection.add(
                maintenance.copy(userId = userId)
            ).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMaintenanceForSailboat(sailboatId: String, limit: Int? = null): Flow<List<Maintenance>> = callbackFlow {
        val query = maintenanceCollection
            .whereEqualTo("sailboatId", sailboatId)
            .whereEqualTo("userId", auth.currentUser?.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .let { q -> limit?.let { q.limit(it.toLong()) } ?: q }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val maintenanceList = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Maintenance::class.java)
            } ?: emptyList()

            trySend(maintenanceList)
        }

        awaitClose { listener.remove() }
    }
}