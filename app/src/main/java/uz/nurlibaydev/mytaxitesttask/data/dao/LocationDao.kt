package uz.nurlibaydev.mytaxitesttask.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import uz.nurlibaydev.mytaxitesttask.data.entity.Location

/**
 *  Created by Nurlibay Koshkinbaev on 11/03/2023 18:43
 */

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addLocation(location: Location)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateLocation(location: Location)

    @Query("SELECT * FROM location_table")
    fun getAllLocation(): Flow<List<Location>>
}