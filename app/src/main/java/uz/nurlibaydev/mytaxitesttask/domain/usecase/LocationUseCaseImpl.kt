package uz.nurlibaydev.mytaxitesttask.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uz.nurlibaydev.mytaxitesttask.data.entity.Location
import uz.nurlibaydev.mytaxitesttask.domain.repository.LocationRepository
import javax.inject.Inject

class LocationUseCaseImpl @Inject constructor(
    private val repository: LocationRepository,
) : LocationUseCase {

    override fun getLastLocation(): Flow<Location> = flow {
        repository.getAllLocations().collect {
            if (it.isNotEmpty()) {
                emit(it.last())
            }
        }
    }
}
