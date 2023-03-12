package uz.nurlibaydev.mytaxitesttask.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.mapbox.mapboxsdk.Mapbox
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import uz.nurlibaydev.mytaxitesttask.BuildConfig
import uz.nurlibaydev.mytaxitesttask.R
import uz.nurlibaydev.mytaxitesttask.utils.Constants.CHANNEL_ID
import uz.nurlibaydev.mytaxitesttask.utils.Constants.CHANNEL_NAME

/**
 *  Created by Nurlibay Koshkinbaev on 08/03/2023 17:11
 */

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this@App
        Mapbox.getInstance(instance, getString(R.string.mapbox_access_token))
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    companion object {
        lateinit var instance: App
    }
}