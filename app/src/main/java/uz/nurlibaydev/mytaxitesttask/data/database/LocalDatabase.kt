package uz.nurlibaydev.mytaxitesttask.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import uz.nurlibaydev.mytaxitesttask.data.dao.LocationDao
import uz.nurlibaydev.mytaxitesttask.data.entity.Location

/**
 *  Created by Nurlibay Koshkinbaev on 11/03/2023 18:41
 */

@Database(entities = [Location::class], version = 2, exportSchema = false)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun getLocationDao(): LocationDao
}