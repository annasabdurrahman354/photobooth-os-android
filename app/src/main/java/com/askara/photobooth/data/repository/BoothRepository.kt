package com.askara.photobooth.data.repository

import com.askara.photobooth.data.model.Booth
import com.askara.photobooth.data.model.Template
import com.askara.photobooth.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BoothRepository {
    private val client = SupabaseClientProvider.client

    suspend fun getBooths(tenantId: String): Result<List<Booth>> = runCatching {
        withContext(Dispatchers.IO) {
            client.postgrest["booths"].select {
                filter { eq("tenant_id", tenantId) }
            }.decodeList<Booth>()
        }
    }

    suspend fun getTemplates(tenantId: String): Result<List<Template>> = runCatching {
        withContext(Dispatchers.IO) {
            client.postgrest["templates"].select {
                filter { eq("tenant_id", tenantId) }
            }.decodeList<Template>()
        }
    }

    suspend fun getBooth(boothId: String): Result<Booth> = runCatching {
        withContext(Dispatchers.IO) {
            client.postgrest["booths"].select {
                filter { eq("id", boothId) }
            }.decodeSingle<Booth>()
        }
    }
}
