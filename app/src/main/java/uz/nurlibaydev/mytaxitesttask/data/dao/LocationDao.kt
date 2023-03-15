package uz.nurlibaydev.mytaxitesttask.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uz.nurlibaydev.mytaxitesttask.data.entity.Location

/**
 *  Created by Nurlibay Koshkinbaev on 11/03/2023 18:43
 */

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLocation(location: Location)

    @Query("SELECT * FROM location_table")
    fun getAllLocation(): Flow<List<Location>>
}
