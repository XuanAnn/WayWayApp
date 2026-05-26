package com.example.waywayapp.ui.auth.login

import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.R
import com.example.waywayapp.ui.theme.AppBg
import com.example.waywayapp.ui.theme.CardWhite
import com.example.waywayapp.ui.theme.ErrorRed
import com.example.waywayapp.ui.theme.Lime
import com.example.waywayapp.ui.theme.SoftWhite
import com.example.waywayapp.ui.theme.TextDark
import com.example.waywayapp.ui.theme.TextGray
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
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember {
        CredentialManager.create(context)
    }
    var isPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) onLoginSuccess(uiState.role)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )

                Text(
                    text = "Good to see you back.",
                    fontSize = 14.sp,
                    color = TextGray
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Image(
                    painter = painterResource(id = R.drawable.your_food_img),
                    contentDescription = null,
                    modifier = Modifier.size(170.dp)
                )

                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(end = 16.dp)
                        .background(Color.White.copy(alpha = 0.25f), CircleShape)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .background(CardWhite)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Let's get something",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    TextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Enter your email", color = TextGray)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = TextGray
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SoftWhite,
                            unfocusedContainerColor = SoftWhite,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            cursorColor = TextDark
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    if (uiState.error != null) {
                        Text(
                            text = "You entered it wrong.",
                            color = ErrorRed,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Enter password", color = TextGray)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = TextGray
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    isPasswordVisible = !isPasswordVisible
                                }
                            ) {
                                Icon(
                                    imageVector = if (isPasswordVisible)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextGray
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SoftWhite,
                            unfocusedContainerColor = SoftWhite,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            cursorColor = TextDark
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = false,
                                onCheckedChange = {},
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Lime,
                                    uncheckedColor = TextGray,
                                    checkmarkColor = TextDark
                                )
                            )

                            Text(
                                text = "Remember me",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }

                        Text(
                            text = "Forgot password?",
                            fontSize = 12.sp,
                            color = TextDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = viewModel::login,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TextDark,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            text = "Sign in",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Or sign in with",
                        color = TextGray,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        LoginSocialIconPlaceholder(
                            text = "f",
                            onClick = {
                                viewModel.setError("Facebook chưa được cấu hình")
                            }
                        )
                        LoginSocialIconPlaceholder(
                            text = "in",
                            onClick = {
                                viewModel.setError("LinkedIn chưa được cấu hình")
                            }
                        )
                        LoginSocialIconPlaceholder(
                            text = "G",
                            onClick = {
                                coroutineScope.launch {
                                    signInWithGoogle(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onToken = viewModel::loginWithGoogleIdToken,
                                        onError = viewModel::setError
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row {
                        Text(
                            text = "Don't have an account? ",
                            color = TextGray,
                            fontSize = 14.sp
                        )

                        Text(
                            text = "SIGN UP",
                            color = TextDark,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { onSignUpClick() }
                        )
                    }
                }
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Lime)
            }
        }
    }
}

@Composable
fun LoginSocialIconPlaceholder(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(SoftWhite)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )
    }
}

private suspend fun signInWithGoogle(
    context: Context,
    credentialManager: CredentialManager,
    onToken: (String) -> Unit,
    onError: (String) -> Unit
) {
    val webClientId = context.getString(R.string.google_web_client_id)
    if (webClientId.isBlank()) {
        onError("Thiếu GOOGLE_WEB_CLIENT_ID trong local.properties")
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val result = credentialManager.getCredential(
            context = context,
            request = request
        )
        val googleCredential =
            GoogleIdTokenCredential.createFrom(result.credential.data)
        onToken(googleCredential.idToken)
    } catch (exception: GetCredentialException) {
        onError(exception.localizedMessage ?: "Đăng nhập Google thất bại")
    } catch (exception: IllegalArgumentException) {
        onError("Không đọc được thông tin đăng nhập Google")
    }
}

private fun Context.findActivityResultRegistryOwner(): ActivityResultRegistryOwner? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is ActivityResultRegistryOwner) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}
