package com.askara.photobooth.data.remote

import android.content.Context
import com.askara.photobooth.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SessionManager
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SupabaseClientProvider {
    private var _client: SupabaseClient? = null
    val client: SupabaseClient
        get() = _client ?: throw IllegalStateException("SupabaseClient not initialized. Call init(context) first.")

    fun init(context: Context) {
        if (_client != null) return
        _client = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Auth) {
                sessionManager = SharedPreferencesSessionManager(context)
                autoLoadFromStorage = true
                alwaysAutoRefresh = true
            }
            install(Postgrest)
            install(Storage)
        }
    }
}

class SharedPreferencesSessionManager(context: Context) : SessionManager {
    private val prefs = context.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }

    override suspend fun saveSession(session: UserSession) {
        prefs.edit().putString("session", json.encodeToString(session)).apply()
    }

    override suspend fun loadSession(): UserSession? {
        val sessionJson = prefs.getString("session", null) ?: return null
        return try {
            json.decodeFromString<UserSession>(sessionJson)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteSession() {
        prefs.edit().remove("session").apply()
    }
}
