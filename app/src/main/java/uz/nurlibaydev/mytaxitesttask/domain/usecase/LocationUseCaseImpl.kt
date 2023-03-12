package uz.nurlibaydev.mytaxitesttask.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.nurlibaydev.mytaxitesttask.data.entity.Location
import uz.nurlibaydev.mytaxitesttask.domain.repository.LocationRepository
import javax.inject.Inject

class LocationUseCaseImpl @Inject constructor(
    private val repository: LocationRepository
) : LocationUseCase {
    override suspend fun addLocation(location: Location) {
        repository.addLocation(location)
    }

    override fun getAllLocations(): Flow<List<Location>> {
        return repository.getAllLocations()
    }
}