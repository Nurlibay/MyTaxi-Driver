package uz.nurlibaydev.mytaxitesttask.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.nurlibaydev.mytaxitesttask.data.entity.Location

interface LocationRepository {

    suspend fun addLocation(location: Location)

    fun getAllLocations(): Flow<List<Location>>
}
