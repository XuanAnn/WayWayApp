package com.example.waywayapp.ui.auth.register

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

// ViewModel xử lý đăng ký bằng OTP trước, sau đó mới điền thông tin cá nhân.
class RegisterViewModel(
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    // State của màn đăng ký, gồm bước OTP và bước hoàn thiện hồ sơ.
    private val _uiState = MutableStateFlow(RegisterState())
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()

    // Cập nhật họ tên sau khi số điện thoại đã xác thực OTP.
    fun onFullNameChange(newValue: String) {
        _uiState.update { it.copy(fullName = newValue, error = null) }
    }

    // Chỉ nhận số tuổi dạng chữ số để lưu vào hồ sơ users.
    fun onAgeChange(newValue: String) {
        _uiState.update { it.copy(age = newValue.filter(Char::isDigit).take(3), error = null) }
    }

    // Email sẽ được link vào tài khoản phone để dùng đăng nhập/quên mật khẩu.
    fun onEmailChange(newValue: String) {
        _uiState.update { it.copy(email = newValue, error = null) }
    }

    // Mật khẩu dùng cho lần đăng nhập email/password sau này.
    fun onPasswordChange(newValue: String) {
        _uiState.update { it.copy(password = newValue, error = null) }
    }

    // Xác nhận mật khẩu để tránh user nhập sai khi đăng ký.
    fun onConfirmPasswordChange(newValue: String) {
        _uiState.update { it.copy(confirmPassword = newValue, error = null) }
    }

    // Cập nhật số điện thoại trước khi gửi OTP.
    fun onPhoneChange(newValue: String) {
        _uiState.update { it.copy(phone = newValue, error = null) }
    }

    // OTP chỉ giữ 6 chữ số theo format Firebase Phone Auth.
    fun onOtpCodeChange(newValue: String) {
        _uiState.update { it.copy(otpCode = newValue.filter(Char::isDigit).take(6), error = null) }
    }

    // Quay lại bước nhập số điện thoại khi user muốn đổi số/gửi lại OTP.
    fun backToPhoneEntry() {
        _uiState.update {
            it.copy(
                otpCode = "",
                verificationId = null,
                isOtpSent = false,
                error = null
            )
        }
    }

    // Gửi OTP qua Firebase, sau khi pass mới cho nhập thông tin cá nhân.
    fun sendOtp(activity: Activity) {
        val phone = normalizePhone(_uiState.value.phone)
        if (phone.isBlank()) {
            _uiState.update { it.copy(error = "Nhập số điện thoại trước.") }
            return
        }

        _uiState.update {
            it.copy(isLoading = true, error = null, phone = phone)
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            // Firebase tự hoàn tất xác thực nếu thiết bị nhận OTP tự động.
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                verifyPhoneCredential(credential)
            }

            // Báo lỗi nếu OTP không gửi được hoặc cấu hình phone auth sai.
            override fun onVerificationFailed(e: FirebaseException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Không gửi được OTP."
                    )
                }
            }

            // Lưu verificationId để bước verifyOtp tạo credential.
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isOtpSent = true,
                        verificationId = verificationId
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

    // Xác minh mã OTP user nhập và kiểm tra phone chưa thuộc tài khoản khác.
    fun verifyOtp() {
        val state = _uiState.value
        val verificationId = state.verificationId
        if (verificationId.isNullOrBlank() || state.otpCode.length < 6) {
            _uiState.update { it.copy(error = "Nhập đủ mã OTP 6 số.") }
            return
        }

        verifyPhoneCredential(PhoneAuthProvider.getCredential(verificationId, state.otpCode))
    }

    // Hoàn tất đăng ký bằng cách link email/password và lưu hồ sơ users.
    fun register() {
        val state = _uiState.value
        if (!state.isPhoneVerified) {
            _uiState.update { it.copy(error = "Vui lòng xác thực OTP trước.") }
            return
        }

        if (state.fullName.isBlank() || state.age.isBlank() || state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Vui lòng điền đầy đủ thông tin.") }
            return
        }

        if (state.password.length < 6) {
            _uiState.update { it.copy(error = "Mật khẩu cần ít nhất 6 ký tự.") }
            return
        }

        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(error = "Mật khẩu không khớp.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                authRepository.completePhoneRegistration(
                    email = state.email.trim(),
                    password = state.password,
                    name = state.fullName.trim(),
                    age = state.age,
                    phone = state.phone
                )
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, isRegisterSuccess = true) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.localizedMessage ?: "Đăng ký thất bại."
                    )
                }
            }
        }
    }

    // Gọi repository để sign in tạm bằng phone và kiểm tra tính duy nhất của số.
    private fun verifyPhoneCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                authRepository.verifyPhoneCredentialForRegistration(credential)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isPhoneVerified = true
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.localizedMessage ?: "Xác thực OTP thất bại."
                    )
                }
            }
        }
    }

    // Chuẩn hoá số điện thoại Việt Nam về +84 trước khi gửi Firebase.
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
