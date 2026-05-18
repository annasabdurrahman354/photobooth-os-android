package com.askara.photobooth.data.repository

import com.askara.photobooth.data.model.Profile
import com.askara.photobooth.data.remote.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {
    private val client = SupabaseClientProvider.client

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }
    }

    suspend fun signOut(): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            client.auth.signOut()
        }
    }

    fun getCurrentUserId(): String? = client.auth.currentUserOrNull()?.id

    suspend fun awaitInitialization() {
        client.auth.awaitInitialization()
    }

    suspend fun getProfile(userId: String): Result<Profile> = runCatching {
        withContext(Dispatchers.IO) {
            client.postgrest["profiles"].select {
                filter { eq("id", userId) }
            }.decodeSingle<Profile>()
        }
    }

    fun isSignedIn(): Boolean = client.auth.currentUserOrNull() != null
}
