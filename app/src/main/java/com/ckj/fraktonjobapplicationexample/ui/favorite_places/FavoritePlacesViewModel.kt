package com.ckj.fraktonjobapplicationexample.ui.favorite_places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ckj.fraktonjobapplicationexample.data.MainRepository
import com.ckj.fraktonjobapplicationexample.data.db.PlaceEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class FavoritePlacesViewModel @Inject constructor(
    mainRepository: MainRepository
): ViewModel() {

    private val places = mainRepository.places

    private val _placesUi = MutableStateFlow(listOf(PlaceEntity()))
    val placesUi: StateFlow<List<PlaceEntity>> = _placesUi

    init {
        viewModelScope.launch {
            places.collect {
                _placesUi.value=it
            }
        }
    }
}