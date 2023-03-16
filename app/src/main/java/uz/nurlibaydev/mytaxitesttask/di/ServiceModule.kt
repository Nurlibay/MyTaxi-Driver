package uz.nurlibaydev.mytaxitesttask.di

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import uz.nurlibaydev.mytaxitesttask.R
import uz.nurlibaydev.mytaxitesttask.service.LocationService
import uz.nurlibaydev.mytaxitesttask.utils.Constants.CHANNEL_ID
import uz.nurlibaydev.mytaxitesttask.utils.Constants.NOTIFICATION_CONTENT_TEXT
import uz.nurlibaydev.mytaxitesttask.utils.Constants.NOTIFICATION_TITLE

/**
 *  Created by Nurlibay Koshkinbaev on 15/03/2023 06:34
 */

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @Provides
    @ServiceScoped
    fun provideMyForegroundService(): LocationService {
        return LocationService()
    }
}
