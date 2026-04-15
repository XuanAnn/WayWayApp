package com.example.waywayapp.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun onEmailChange(newValue: String) {
        _uiState.update { it.copy(email = newValue, error = null) }
    }

    fun onPasswordChange(newValue: String) {
        _uiState.update { it.copy(password = newValue, error = null) }
    }

    fun login() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Vui lòng nhập đầy đủ thông tin") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = task.exception?.message ?: "Đăng nhập thất bại") }
                }
            }
    }
}
