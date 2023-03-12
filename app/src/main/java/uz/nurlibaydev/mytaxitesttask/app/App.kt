package uz.nurlibaydev.mytaxitesttask.app

import android.app.Application
import com.mapbox.mapboxsdk.Mapbox
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import uz.nurlibaydev.mytaxitesttask.BuildConfig
import uz.nurlibaydev.mytaxitesttask.R

/**
 *  Created by Nurlibay Koshkinbaev on 08/03/2023 17:11
 */

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}