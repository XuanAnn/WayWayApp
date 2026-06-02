package com.example.waywayapp.ui.auth.login

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.R
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
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (String) -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val activityResultOwner = remember(activity) { activity as? ActivityResultRegistryOwner }
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val callbackManager = remember { CallbackManager.Factory.create() }

    DisposableEffect(callbackManager) {
        LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    viewModel.loginWithFacebookAccessToken(result.accessToken.token)
                }

                override fun onCancel() = Unit

                override fun onError(error: FacebookException) {
                    viewModel.setError(error.localizedMessage ?: "Đăng nhập Facebook thất bại")
                }
            }
        )

        onDispose {
            LoginManager.getInstance().unregisterCallback(callbackManager)
        }
    }

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) onLoginSuccess(uiState.role)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
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
            if (uiState.isOtpSent) {
                AuthOtpScreenContent(
                    phone = uiState.phone,
                    code = uiState.otpCode,
                    onCodeChange = viewModel::onOtpCodeChange,
                    onBackClick = viewModel::backToPhoneEntry,
                    onVerifyClick = viewModel::verifyOtp,
                    onResendClick = {
                        val hostActivity = activity
                        if (hostActivity == null) {
                            viewModel.setError("Không mở được OTP trên màn này")
                        } else {
                            viewModel.sendOtp(hostActivity)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                return@Column
            }

            if (uiState.isForgotPasswordMode) {
                AuthBackButton(onClick = viewModel::backToPhoneEntry)

                Spacer(modifier = Modifier.height(24.dp))

                AuthTitle(
                    title = "Forgot Password?",
                    subtitle = "Enter the email linked after OTP registration. We'll send a reset link to that email."
                )

                Spacer(modifier = Modifier.height(24.dp))

                AuthFieldLabel("Email")
                AuthTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = "Email",
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(18.dp))

                AuthPrimaryButton(
                    text = "Send reset link",
                    onClick = viewModel::sendPasswordReset
                )

                Spacer(modifier = Modifier.height(24.dp))
                return@Column
            }

            AuthBackButton(onClick = {})

            Spacer(modifier = Modifier.height(24.dp))

            AuthTitle(
                title = "Welcome Back! 👋",
                subtitle = "Enter your phone number and use the test OTP to sign in."
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthFieldLabel("Phone Number")
            AuthTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                placeholder = "Phone Number",
                keyboardType = KeyboardType.Phone,
                leading = { AuthPhoneLeading() }
            )

            Spacer(modifier = Modifier.height(18.dp))

            AuthPrimaryButton(
                text = "Send OTP",
                onClick = {
                    val hostActivity = activity
                    if (hostActivity == null) {
                        viewModel.setError("Không mở được OTP trên màn này")
                    } else {
                        viewModel.sendOtp(hostActivity)
                    }
                }
            )

            androidx.compose.material3.Text(
                text = "Forgot password?",
                color = AuthGreen,
                fontSize = 11.sp,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clickable { viewModel.openForgotPassword() }
            )

            Spacer(modifier = Modifier.height(22.dp))

            AuthInlineLink(
                prefix = "Don't have an account? ",
                action = "Sign up",
                onClick = onSignUpClick
            )

            Spacer(modifier = Modifier.height(22.dp))
            AuthDivider()
            Spacer(modifier = Modifier.height(16.dp))

            AuthSocialButton(AuthProvider.Google) {
                scope.launch {
                    signInWithGoogle(
                        context = context,
                        credentialManager = credentialManager,
                        onToken = viewModel::loginWithGoogleIdToken,
                        onError = viewModel::setError
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            AuthSocialButton(AuthProvider.Apple) {}
            Spacer(modifier = Modifier.height(10.dp))

            AuthSocialButton(AuthProvider.Facebook) {
                if (activity == null || activityResultOwner == null) {
                    viewModel.setError("Không mở được Facebook login trên màn này")
                    return@AuthSocialButton
                }

                runCatching {
                    if (!FacebookSdk.isInitialized()) {
                        FacebookSdk.sdkInitialize(activity.applicationContext)
                    }
                    LoginManager.getInstance().logOut()
                    LoginManager.getInstance().logInWithReadPermissions(
                        activityResultOwner,
                        callbackManager,
                        listOf("public_profile")
                    )
                }.onFailure {
                    viewModel.setError(it.localizedMessage ?: "Không mở được Facebook login")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            AuthSocialButton(AuthProvider.X) {}
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

private suspend fun signInWithGoogle(
    context: Context,
    credentialManager: CredentialManager,
    onToken: (String) -> Unit,
    onError: (String) -> Unit
) {
    val serverClientId = context.getString(R.string.google_web_client_id)
    if (serverClientId.isBlank()) {
        onError("Thiếu GOOGLE_WEB_CLIENT_ID trong local.properties")
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(serverClientId)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    runCatching {
        credentialManager.getCredential(context, request).credential
    }.onSuccess { credential ->
        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            onToken(googleCredential.idToken)
        } else {
            onError("Google credential không hợp lệ")
        }
    }.onFailure { throwable ->
        val message = when (throwable) {
            is GetCredentialException -> throwable.localizedMessage ?: "Không thể đăng nhập Google"
            else -> throwable.localizedMessage ?: "Không thể đăng nhập Google"
        }
        onError(message)
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
