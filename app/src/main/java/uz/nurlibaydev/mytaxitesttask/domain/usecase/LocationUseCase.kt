package uz.nurlibaydev.mytaxitesttask.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.nurlibaydev.mytaxitesttask.data.entity.Location

interface LocationUseCase {

    suspend fun addLocation(location: Location)

    fun getLastLocation(): Flow<Location>
}
