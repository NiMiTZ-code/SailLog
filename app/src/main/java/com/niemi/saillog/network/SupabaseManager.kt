package com.niemi.saillog.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import com.niemi.saillog.BuildConfig
import kotlin.time.Duration.Companion.seconds

object SupabaseManager {
    private const val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private const val SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Storage){
            transferTimeout = 90.seconds
        }

    }
}