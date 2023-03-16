package uz.nurlibaydev.mytaxitesttask.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.nurlibaydev.mytaxitesttask.data.repository.LocationRepository
import uz.nurlibaydev.mytaxitesttask.data.repository.impl.LocationRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository
}
