package com.ckj.fraktonjobapplicationexample.ui.add_place

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ckj.fraktonjobapplicationexample.data.MainRepository
import com.ckj.fraktonjobapplicationexample.data.db.PlaceEntity
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddNewPlaceViewModel @Inject constructor(private val mainRepository: MainRepository) :
    ViewModel() {
    private var _imageUri: MutableLiveData<String> = MutableLiveData("")
    val imageUri: LiveData<String> get() = _imageUri


    private val _placeStatusUiState: MutableLiveData<PlaceStatusUiState> =
        MutableLiveData()
    val placeStatusUiState: LiveData<PlaceStatusUiState> get() = _placeStatusUiState

    fun setImageUri(uri: String?) {
        _imageUri.value = uri
    }

    private fun savePlace(placeEntity: PlaceEntity) {
        viewModelScope.launch {
                mainRepository.insertPlace(placeEntity)
        }
    }

    fun onUploadTaskError(it: Exception) {
        _placeStatusUiState.value=PlaceStatusUiState.Error(it)
    }

    fun onUploadTaskSuccess( placeEntity: PlaceEntity) {
        savePlace(placeEntity)
        _placeStatusUiState.value=PlaceStatusUiState.Success(placeEntity)
     }

    fun onLoadingTask() {
        _placeStatusUiState.value=PlaceStatusUiState.Loading
    }
}

sealed class PlaceStatusUiState {
    class Success(data:PlaceEntity) : PlaceStatusUiState()
    class Error(e:Exception) : PlaceStatusUiState()
    object Loading : PlaceStatusUiState()

}