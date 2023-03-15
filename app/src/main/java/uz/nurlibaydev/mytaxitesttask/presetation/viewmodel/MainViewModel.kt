package uz.nurlibaydev.mytaxitesttask.presetation.viewmodel

import kotlinx.coroutines.flow.SharedFlow
import uz.nurlibaydev.mytaxitesttask.data.entity.Location

interface MainViewModel {

    val lastLocation: SharedFlow<Location>

    fun addLocation(location: Location)

    fun getLastLocation()
}