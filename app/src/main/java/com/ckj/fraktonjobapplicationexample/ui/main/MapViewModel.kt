package com.ckj.fraktonjobapplicationexample.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ckj.fraktonjobapplicationexample.data.MainRepository
import com.ckj.fraktonjobapplicationexample.data.db.PlaceEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class MapViewModel @Inject constructor(
    mainRepository: MainRepository
) : ViewModel() {

    private val places = mainRepository.places

    private val _placesUi = MutableStateFlow(listOf(PlaceEntity()))
    val placesUi: StateFlow<List<PlaceEntity>> = _placesUi



    fun onMapReady() {

        viewModelScope.launch {
            places.collect {
                _placesUi.value=it
            }
        }

    }


}