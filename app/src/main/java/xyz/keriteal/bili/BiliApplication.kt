package xyz.keriteal.bili

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BiliApplication : Application() {
    companion object {
        lateinit var instance: BiliApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}