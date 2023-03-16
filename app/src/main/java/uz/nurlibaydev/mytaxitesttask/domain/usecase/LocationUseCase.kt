package uz.nurlibaydev.mytaxitesttask.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.nurlibaydev.mytaxitesttask.data.entity.Location

interface LocationUseCase {

    fun getLastLocation(): Flow<Location>
}
