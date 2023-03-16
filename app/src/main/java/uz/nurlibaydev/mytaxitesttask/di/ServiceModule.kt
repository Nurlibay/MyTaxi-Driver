package uz.nurlibaydev.mytaxitesttask.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
import uz.nurlibaydev.mytaxitesttask.service.LocationService

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
