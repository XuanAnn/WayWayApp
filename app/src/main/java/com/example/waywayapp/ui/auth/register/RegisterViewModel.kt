 package com.example.waywayapp.ui.auth.register

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RegisterViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(RegisterState())
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()

    fun onEmailChange(newValue: String) {
        _uiState.update { it.copy(email = newValue) }
    }

    fun onPasswordChange(newValue: String) {
        _uiState.update { it.copy(password = newValue) }
    }

    fun onConfirmPasswordChange(newValue: String) {
        _uiState.update { it.copy(confirmPassword = newValue) }
    }

    fun onPhoneChange(newValue: String) {
        _uiState.update { it.copy(phone = newValue) }
    }

    fun register() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Vui lòng điền đầy đủ thông tin") }
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(error = "Mật khẩu không khớp") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        auth.createUserWithEmailAndPassword(state.email, state.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, isRegisterSuccess = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = task.exception?.message) }
                }
            }
    }
}
