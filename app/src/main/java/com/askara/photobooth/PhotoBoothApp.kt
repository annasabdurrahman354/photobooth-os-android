package com.askara.photobooth

import android.app.Application
import com.askara.photobooth.data.remote.SupabaseClientProvider

class PhotoBoothApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseClientProvider.init(this)
    }
}
