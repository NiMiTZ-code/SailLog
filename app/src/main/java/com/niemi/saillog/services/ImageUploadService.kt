package com.niemi.saillog.services

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.niemi.saillog.network.SupabaseManager
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class ImageUploadService(private val context: Context) {

    companion object {
        private const val BUCKET_NAME = "sailboat-images"
        private const val SIGNED_URL_EXPIRY = 3600 // 1 hour in seconds
    }

    /**
     * Upload image to Supabase storage
     * Returns the storage path for saving in Firestore
     */
    suspend fun uploadSailboatImage(imageUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext null

            // Convert URI to byte array
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes() ?: return@withContext null
            inputStream.close()

            // Generate unique filename
            val fileName = "${UUID.randomUUID()}.jpg"
            val storagePath = "$userId/$fileName"

            // Upload to Supabase
            SupabaseManager.client.storage
                .from(BUCKET_NAME)
                .upload(storagePath, bytes){
                    upsert = false
                }

            // Return the storage path (not the URL)
            return@withContext storagePath
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    /**
     * Get signed URL for private bucket image
     */
    suspend fun getSignedImageUrl(storagePath: String): String? = withContext(Dispatchers.IO) {
        try {
            val signedUrl = SupabaseManager.client.storage
                .from(BUCKET_NAME)
                .createSignedUrl(storagePath, expiresIn = SIGNED_URL_EXPIRY.toDuration(DurationUnit.SECONDS))

            return@withContext signedUrl
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    /**
     * Delete image from Supabase storage
     */
    suspend fun deleteImage(storagePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            SupabaseManager.client.storage
                .from(BUCKET_NAME)
                .delete(listOf(storagePath))

            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}