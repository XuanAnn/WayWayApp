package com.example.waywayapp.ui.user.booking.express

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExpressViewModel : ViewModel(){
    private val _uiState = MutableStateFlow(ExpressState())
    val uiState: StateFlow<ExpressState> = _uiState.asStateFlow()
}
