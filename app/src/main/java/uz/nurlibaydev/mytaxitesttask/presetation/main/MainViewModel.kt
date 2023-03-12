package uz.nurlibaydev.mytaxitesttask.presetation.main

import kotlinx.coroutines.flow.SharedFlow
import uz.nurlibaydev.mytaxitesttask.data.entity.Location

interface MainViewModel {

    val allUserLocations: SharedFlow<List<Location>>

    fun addLocation(location: Location)

    fun getAllLocations()
}