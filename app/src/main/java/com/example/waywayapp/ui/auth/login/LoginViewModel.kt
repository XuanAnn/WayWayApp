package com.example.waywayapp.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.data.repository.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun onEmailChange(newValue: String) {
        _uiState.update { it.copy(email = newValue, error = null) }
    }

    fun onPasswordChange(newValue: String) {
        _uiState.update { it.copy(password = newValue, error = null) }
    }

    fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(error = "Vui lòng nhập đầy đủ thông tin")
            }
            return
        }

        runAuth {
            authRepository.signInWithEmail(email, password)
        }
    }

    fun loginWithGoogleIdToken(
        idToken: String
    ) {
        runAuth {
            authRepository.signInWithGoogleIdToken(idToken)
        }
    }

    fun loginWithFacebookAccessToken(
        accessToken: String
    ) {
        runAuth {
            authRepository.signInWithFacebookAccessToken(accessToken)
        }
    }

    fun setError(
        message: String
    ) {
        _uiState.update {
            it.copy(isLoading = false, error = message)
        }
    }

    private fun runAuth(
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null)
            }

            runCatching {
                block()
            }.onSuccess {
                val role = runCatching {
                    authRepository.getCurrentUserRole()
                }.getOrDefault("USER")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        role = role
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.localizedMessage ?: "Đăng nhập thất bại"
                    )
                }
            }
        }
    }
}
