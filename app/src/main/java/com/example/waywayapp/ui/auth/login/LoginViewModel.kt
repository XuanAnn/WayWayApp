package com.example.waywayapp.ui.auth.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.data.repository.FirebaseAuthRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// ViewModel xử lý đăng nhập email/password, Google, Facebook, OTP và quên mật khẩu.
class LoginViewModel(
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    // State của màn login, chứa input, loading, lỗi và role sau khi đăng nhập.
    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    // Cập nhật email đăng nhập và xoá lỗi cũ trên UI.
    fun onEmailChange(newValue: String) {
        _uiState.update { it.copy(email = newValue, error = null, message = null) }
    }

    // Cập nhật mật khẩu đăng nhập.
    fun onPasswordChange(newValue: String) {
        _uiState.update { it.copy(password = newValue, error = null, message = null) }
    }

    // Cập nhật số điện thoại dùng cho luồng OTP.
    fun onPhoneChange(newValue: String) {
        _uiState.update { it.copy(phone = newValue, error = null, message = null) }
    }

    // Chỉ nhận tối đa 6 chữ số OTP để khớp Firebase Phone Auth.
    fun onOtpCodeChange(newValue: String) {
        _uiState.update { it.copy(otpCode = newValue.filter(Char::isDigit).take(6), error = null, message = null) }
    }

    // Quay lại bước nhập số điện thoại khi user muốn gửi lại OTP.
    fun backToPhoneEntry() {
        _uiState.update {
            it.copy(
                otpCode = "",
                verificationId = null,
                isOtpSent = false,
                isForgotPasswordMode = false,
                error = null,
                message = null
            )
        }
    }

    // Mở mode quên mật khẩu, dùng email để gửi reset link Firebase.
    fun openForgotPassword() {
        _uiState.update {
            it.copy(
                isForgotPasswordMode = true,
                isOtpSent = false,
                otpCode = "",
                verificationId = null,
                error = null,
                message = null
            )
        }
    }

    // Gửi OTP bằng Firebase Phone Auth, ở dev có thể dùng số test.
    fun sendOtp(activity: Activity) {
        val phone = normalizePhone(_uiState.value.phone)
        if (phone.isBlank()) {
            _uiState.update { it.copy(error = "Nhập số điện thoại trước.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null, message = null, phone = phone) }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            // Firebase tự xác thực khi thiết bị nhận OTP tự động.
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneCredential(credential)
            }

            // Báo lỗi khi Firebase không gửi/không xác thực được OTP.
            override fun onVerificationFailed(e: FirebaseException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Không gửi được OTP."
                    )
                }
            }

            // Lưu verificationId để bước nhập mã OTP tạo credential.
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isOtpSent = true,
                        verificationId = verificationId,
                        message = "Đã gửi OTP test/dev."
                    )
                }
            }
        }

        val options = PhoneAuthOptions.newBuilder()
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Tạo PhoneAuthCredential từ verificationId và OTP user nhập.
    fun verifyOtp() {
        val state = _uiState.value
        val verificationId = state.verificationId
        if (verificationId.isNullOrBlank() || state.otpCode.length < 6) {
            _uiState.update { it.copy(error = "Nhập đủ mã OTP 6 số.") }
            return
        }

        val credential = PhoneAuthProvider.getCredential(verificationId, state.otpCode)
        signInWithPhoneCredential(credential)
    }

    // Đăng nhập bằng email/password qua FirebaseAuthRepository.
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

    // Gửi email reset password bằng Firebase Authentication.
    fun sendPasswordReset() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.update {
                it.copy(error = "Nhập email trước khi đặt lại mật khẩu")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null, message = null)
            }

            runCatching {
                authRepository.sendPasswordResetEmail(email)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Đã gửi email đặt lại mật khẩu"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.localizedMessage ?: "Không gửi được email đặt lại mật khẩu"
                    )
                }
            }
        }
    }

    // Đăng nhập Google bằng idToken lấy từ Google Sign-In.
    fun loginWithGoogleIdToken(
        idToken: String
    ) {
        runAuth {
            authRepository.signInWithGoogleIdToken(idToken)
        }
    }

    // Đăng nhập Facebook bằng accessToken từ Facebook SDK.
    fun loginWithFacebookAccessToken(
        accessToken: String
    ) {
        runAuth {
            authRepository.signInWithFacebookAccessToken(accessToken)
        }
    }

    // Đăng nhập bằng credential OTP rồi lấy role từ Firestore.
    private fun signInWithPhoneCredential(
        credential: PhoneAuthCredential
    ) {
        runAuth {
            authRepository.signInWithPhoneCredential(credential)
        }
    }

    // Nhận lỗi từ UI layer, ví dụ lỗi Google/Facebook SDK.
    fun setError(
        message: String
    ) {
        _uiState.update {
            it.copy(isLoading = false, error = message, message = null)
        }
    }

    // Chạy một luồng auth chung, sau đó đọc role để điều hướng Home/Admin/Driver.
    private fun runAuth(
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null, message = null)
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

    // Chuẩn hoá số Việt Nam về +84 để Firebase và Firestore dùng cùng một định dạng.
    private fun normalizePhone(value: String): String {
        val raw = value.trim().replace(" ", "")
        return when {
            raw.isBlank() -> ""
            raw.startsWith("+") -> raw
            raw.startsWith("0") -> "+84${raw.drop(1)}"
            else -> raw
        }
    }
}
