package com.example.waywayapp.ui.auth.register

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.ui.auth.common.AuthAgreeRow
import com.example.waywayapp.ui.auth.common.AuthBackButton
import com.example.waywayapp.ui.auth.common.AuthDivider
import com.example.waywayapp.ui.auth.common.AuthFieldLabel
import com.example.waywayapp.ui.auth.common.AuthGreen
import com.example.waywayapp.ui.auth.common.AuthInlineLink
import com.example.waywayapp.ui.auth.common.AuthMuted
import com.example.waywayapp.ui.auth.common.AuthOtpScreenContent
import com.example.waywayapp.ui.auth.common.AuthPhoneLeading
import com.example.waywayapp.ui.auth.common.AuthPrimaryButton
import com.example.waywayapp.ui.auth.common.AuthProvider
import com.example.waywayapp.ui.auth.common.AuthSocialButton
import com.example.waywayapp.ui.auth.common.AuthTextField
import com.example.waywayapp.ui.auth.common.AuthTitle

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var agreed by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isRegisterSuccess) {
        if (uiState.isRegisterSuccess) onRegisterSuccess()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            AuthBackButton(onBackClick)

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isOtpSent && !uiState.isPhoneVerified) {
                AuthOtpScreenContent(
                    phone = uiState.phone,
                    code = uiState.otpCode,
                    onCodeChange = viewModel::onOtpCodeChange,
                    onBackClick = viewModel::backToPhoneEntry,
                    onVerifyClick = viewModel::verifyOtp,
                    onResendClick = {
                        val hostActivity = activity
                        if (hostActivity == null) {
                            Toast.makeText(context, "Không mở được OTP trên màn này", Toast.LENGTH_LONG).show()
                        } else {
                            viewModel.sendOtp(hostActivity)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                return@Column
            }

            AuthTitle(
                title = if (uiState.isPhoneVerified) "Fill Personal Info" else "Join WayWay Today ✨",
                subtitle = if (uiState.isPhoneVerified) {
                    "Phone verified. Add email and password so password reset can work later."
                } else {
                    "Let's get started. Enter your phone number to create your WayWay account."
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            if (!uiState.isPhoneVerified) {
                AuthFieldLabel("Phone Number")
                AuthTextField(
                    value = uiState.phone,
                    onValueChange = viewModel::onPhoneChange,
                    placeholder = "Phone Number",
                    keyboardType = KeyboardType.Phone,
                    leading = { AuthPhoneLeading() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                AuthAgreeRow(
                    checked = agreed,
                    onCheckedChange = { agreed = it },
                    text = "I agree to WayWay ",
                    linkText = "Terms & Conditions."
                )

                Spacer(modifier = Modifier.height(20.dp))

                AuthPrimaryButton(
                    text = "Send OTP",
                    enabled = agreed,
                    onClick = {
                        val hostActivity = activity
                        if (hostActivity == null) {
                            Toast.makeText(context, "Không mở được OTP trên màn này", Toast.LENGTH_LONG).show()
                        } else {
                            viewModel.sendOtp(hostActivity)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                AuthInlineLink(
                    prefix = "Already have an account? ",
                    action = "Sign in",
                    onClick = onBackClick
                )

                Spacer(modifier = Modifier.height(22.dp))
                AuthDivider()
                Spacer(modifier = Modifier.height(16.dp))

                AuthSocialButton(AuthProvider.Google) {}
                Spacer(modifier = Modifier.height(10.dp))
                AuthSocialButton(AuthProvider.Apple) {}
                Spacer(modifier = Modifier.height(10.dp))
                AuthSocialButton(AuthProvider.Facebook) {}
                Spacer(modifier = Modifier.height(10.dp))
                AuthSocialButton(AuthProvider.X) {}
            } else {
                AuthFieldLabel("Full Name")
                AuthTextField(
                    value = uiState.fullName,
                    onValueChange = viewModel::onFullNameChange,
                    placeholder = "Full Name"
                )

                Spacer(modifier = Modifier.height(10.dp))
                AuthFieldLabel("Age")
                AuthTextField(
                    value = uiState.age,
                    onValueChange = viewModel::onAgeChange,
                    placeholder = "Age",
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(10.dp))
                AuthFieldLabel("Email")
                AuthTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = "Email",
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(10.dp))
                AuthFieldLabel("Password")
                AuthTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = "Password",
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailing = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = AuthMuted
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))
                AuthFieldLabel("Confirm Password")
                AuthTextField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    placeholder = "Confirm Password",
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailing = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                imageVector = if (confirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = AuthMuted
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                AuthPrimaryButton(
                    text = "Sign up",
                    onClick = viewModel::register
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AuthGreen)
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
