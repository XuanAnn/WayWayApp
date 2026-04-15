package com.example.waywayapp.ui.auth.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.R
import com.example.waywayapp.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background( brush = Brush.verticalGradient(
                colors = listOf(GoFoodBg, Color.White)
            ))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Section

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoFoodTextDark
                )
                Text(
                    text = "Good to see you back.",
                    fontSize = 14.sp,
                    color = GoFoodTextGray
                )
            }

            // Top Illustration (Food/Drink)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                contentAlignment = Alignment.CenterEnd
            ) {

                 Image(painter = painterResource(id = R.drawable.your_food_img), contentDescription = null)
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(end = 16.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("", fontSize = 60.sp) // Emoji as temporary placeholder
                }
            }

            // Main Form Container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .background(GoFoodSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Let's get something",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoFoodTextDark,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Email Field
                    TextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your email", color = GoFoodTextGray) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GoFoodTextGray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = LightGray.copy(alpha = 0.5f),
                            unfocusedContainerColor = LightGray.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (uiState.error != null) {
                        Text(
                            text = "You entered it wrong.",
                            color = GoFoodError,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    TextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter password", color = GoFoodTextGray) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoFoodTextGray) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = GoFoodTextGray
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = LightGray.copy(alpha = 0.5f),
                            unfocusedContainerColor = LightGray.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = false, onCheckedChange = {})
                            Text(text = "Reminder me next time", fontSize = 12.sp, color = GoFoodTextGray)
                        }
                        Text(
                            text = "Forgot password?",
                            fontSize = 12.sp,
                            color = MofinowBlue,
                            modifier = Modifier.clickable { }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sign In Button
                    Button(
                        onClick = viewModel::login,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoFoodGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Sign in", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(text = "Or sign in with", color = GoFoodTextGray, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        SocialIconPlaceholder("f")
                        SocialIconPlaceholder("in")
                        SocialIconPlaceholder("G")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row {
                        Text(text = "Don't have an account? ", color = GoFoodTextGray, fontSize = 14.sp)
                        Text(
                            text = "SIGN UP",
                            color = GoFoodGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { onSignUpClick() }
                        )
                    }
                }
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoFoodGreen)
            }
        }
    }
}

@Composable
fun SocialIconPlaceholder(text: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(LightGray.copy(alpha = 0.3f))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        // PLACEHOLDER FOR SOCIAL ICONS
        Text(text = text, fontWeight = FontWeight.Bold, color = MofinowBlue)
    }
}
