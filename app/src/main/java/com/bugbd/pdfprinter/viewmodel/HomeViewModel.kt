package com.bugbd.pdfprinter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    var _cameraState = MutableStateFlow("None")

    var cameraState:StateFlow<String> = _cameraState

    fun setCameraState(value: String){
        //Utils.showLog(Utils.logTag_Doc,"setCameraState()")
        _cameraState.value = value
    }


    init {
        viewModelScope.launch {
            delay(300)
            _isLoading.value = false
        }
    }
}

enum class CameraUtils{
    None,
    TapToStartButton,
}