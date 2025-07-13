package com.niemi.saillog.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.niemi.saillog.data.Sailboat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SailboatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Get current user's sailboats
    fun getUserSailboats(): Flow<List<Sailboat>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

        val listener = firestore.collection("sailboats")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val sailboats = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Sailboat::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(sailboats)
            }

        awaitClose { listener.remove() }
    }

    // Get a specific sailboat
    suspend fun getSailboat(sailboatId: String): Sailboat? {
        return try {
            firestore.collection("sailboats")
                .document(sailboatId)
                .get()
                .await()
                .toObject(Sailboat::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Add or update sailboat
    suspend fun saveSailboat(sailboat: Sailboat): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            val sailboatToSave = sailboat.copy(userId = userId)

            if (sailboat.id.isEmpty()) {
                // New sailboat
                firestore.collection("sailboats")
                    .add(sailboatToSave)
                    .await()
            } else {
                // Update existing
                firestore.collection("sailboats")
                    .document(sailboat.id)
                    .set(sailboatToSave)
                    .await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}