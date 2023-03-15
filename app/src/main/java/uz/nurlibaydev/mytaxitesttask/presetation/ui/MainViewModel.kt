package uz.nurlibaydev.mytaxitesttask.presetation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uz.nurlibaydev.mytaxitesttask.data.entity.Location
import uz.nurlibaydev.mytaxitesttask.domain.usecase.LocationUseCase
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val useCase: LocationUseCase,
) : ViewModel() {

    val lastLocation = MutableSharedFlow<Location>()

    fun addLocation(location: Location) {
        viewModelScope.launch(Dispatchers.IO) {
            useCase.addLocation(location)
        }
    }

    fun getLastLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            useCase.getLastLocation().collectLatest {
                lastLocation.emit(it)
            }
        }
    }
}
