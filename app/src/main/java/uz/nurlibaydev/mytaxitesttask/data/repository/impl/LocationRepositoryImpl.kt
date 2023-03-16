package uz.nurlibaydev.mytaxitesttask.data.repository.impl

import kotlinx.coroutines.flow.Flow
import uz.nurlibaydev.mytaxitesttask.data.dao.LocationDao
import uz.nurlibaydev.mytaxitesttask.data.entity.Location
import uz.nurlibaydev.mytaxitesttask.data.repository.LocationRepository
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val dao: LocationDao,
) : LocationRepository {
    override suspend fun addLocation(location: Location) {
        dao.addLocation(location)
    }

    override fun getAllLocations(): Flow<List<Location>> {
        return dao.getAllLocation()
    }
}
