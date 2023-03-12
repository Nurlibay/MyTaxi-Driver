package uz.nurlibaydev.mytaxitesttask.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.nurlibaydev.mytaxitesttask.domain.repository.LocationRepository
import uz.nurlibaydev.mytaxitesttask.domain.repository.LocationRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository
}