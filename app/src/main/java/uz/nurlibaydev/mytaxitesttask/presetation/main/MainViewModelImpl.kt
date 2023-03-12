package uz.nurlibaydev.mytaxitesttask.presetation.main

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
class MainViewModelImpl @Inject constructor(
    private val useCase: LocationUseCase,
) : MainViewModel, ViewModel() {
    override val allUserLocations = MutableSharedFlow<List<Location>>()

    override fun addLocation(location: Location) {
        viewModelScope.launch(Dispatchers.IO) {
            useCase.addLocation(location)
        }
    }

    override fun getAllLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            useCase.getAllLocations().collectLatest {
                allUserLocations.emit(it)
            }
        }
    }
}