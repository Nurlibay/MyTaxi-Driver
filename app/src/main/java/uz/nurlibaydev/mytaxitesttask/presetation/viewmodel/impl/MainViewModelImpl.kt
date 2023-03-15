package uz.nurlibaydev.mytaxitesttask.presetation.viewmodel.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uz.nurlibaydev.mytaxitesttask.data.entity.Location
import uz.nurlibaydev.mytaxitesttask.domain.usecase.LocationUseCase
import uz.nurlibaydev.mytaxitesttask.presetation.viewmodel.MainViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModelImpl @Inject constructor(
    private val useCase: LocationUseCase,
) : MainViewModel, ViewModel() {
    override val lastLocation = MutableSharedFlow<Location>()

    override fun addLocation(location: Location) {
        viewModelScope.launch(Dispatchers.IO) {
            useCase.addLocation(location)
        }
    }

    override fun getLastLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            useCase.getLastLocation().collectLatest {
                lastLocation.emit(it)
            }
        }
    }
}