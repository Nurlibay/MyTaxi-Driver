package uz.nurlibaydev.mytaxitesttask.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uz.nurlibaydev.mytaxitesttask.data.dao.LocationDao
import uz.nurlibaydev.mytaxitesttask.data.database.LocalDatabase
import javax.inject.Singleton

/**
 *  Created by Nurlibay Koshkinbaev on 27/09/2022 15:30
 */

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @[Provides Singleton]
    fun provideDatabase(@ApplicationContext context: Context): LocalDatabase {
        return Room.databaseBuilder(
            context,
            LocalDatabase::class.java,
            "location_data.db",
        )
            .build()
    }

    @[Provides Singleton]
    fun provideLocationDao(localDatabase: LocalDatabase): LocationDao {
        return localDatabase.getLocationDao()
    }
}
