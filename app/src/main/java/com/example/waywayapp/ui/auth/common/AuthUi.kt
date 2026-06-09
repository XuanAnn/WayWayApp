package com.example.waywayapp.ui.auth.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.R
import com.example.waywayapp.ui.theme.WayWayOnSurface
import com.example.waywayapp.ui.theme.WayWayOnSurfaceVariant
import com.example.waywayapp.ui.theme.WayWayOutlineVariant
import com.example.waywayapp.ui.theme.WayWayPrimary
import com.example.waywayapp.ui.theme.WayWaySurface

val AuthGreen = WayWayPrimary
val AuthDarkGreen = WayWayPrimary
val AuthInk = WayWayOnSurface
val AuthMuted = WayWayOnSurfaceVariant
val AuthLine = WayWayOutlineVariant
val AuthField = WayWaySurface
val AuthFont = FontFamily.SansSerif

@Composable
fun AuthBackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = AuthInk,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun AuthTitle(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            color = AuthInk,
            fontFamily = AuthFont,
            fontSize = 21.sp,
            lineHeight = 25.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.sp
        )
        Text(
            text = subtitle,
            color = AuthMuted,
            fontFamily = AuthFont,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun AuthFieldLabel(text: String) {
    Text(
        text = text,
        color = AuthInk,
        fontFamily = AuthFont,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leading: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        textStyle = TextStyle(
            color = AuthInk,
            fontFamily = AuthFont,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        ),
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFFB7BEBC),
                fontFamily = AuthFont,
                fontSize = 12.sp,
                letterSpacing = 0.sp
            )
        },
        leadingIcon = leading,
        trailingIcon = trailing,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AuthField,
            unfocusedBorderColor = AuthField,
            focusedContainerColor = AuthField,
            unfocusedContainerColor = AuthField,
            cursorColor = AuthGreen
        )
    )
}

@Composable
fun AuthPhoneLeading() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(start = 2.dp)
    ) {
        Text("🇺🇸", fontSize = 15.sp)
        Text("⌄", color = AuthMuted, fontSize = 11.sp, fontFamily = AuthFont)
    }
}

@Composable
fun AuthAgreeRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    linkText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(if (checked) AuthGreen else Color.Transparent)
                .border(1.6.dp, AuthGreen, RoundedCornerShape(3.dp))
                .clickable { onCheckedChange(!checked) },
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
            }
        }
        Row(modifier = Modifier.padding(start = 12.dp)) {
            Text(text, color = AuthInk, fontSize = 10.sp, fontFamily = AuthFont, letterSpacing = 0.sp)
            Text(linkText, color = AuthGreen, fontSize = 10.sp, fontFamily = AuthFont, letterSpacing = 0.sp)
        }
    }
}

@Composable
fun AuthInlineLink(
    prefix: String,
    action: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(prefix, color = AuthMuted, fontSize = 11.sp, fontFamily = AuthFont, letterSpacing = 0.sp)
        Text(
            text = action,
            color = AuthGreen,
            fontSize = 11.sp,
            fontFamily = AuthFont,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
            modifier = Modifier.clickable(onClick = onClick)
        )
    }
}

@Composable
fun AuthDivider() {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f).height(1.dp).background(AuthLine))
        Text(
            text = "or",
            color = AuthMuted,
            fontSize = 10.sp,
            fontFamily = AuthFont,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Box(modifier = Modifier.weight(1f).height(1.dp).background(AuthLine))
    }
}

@Composable
fun AuthSocialButton(
    provider: AuthProvider,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(100.dp),
        color = Color.White,
        border = BorderStroke(1.dp, AuthLine),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProviderIcon(provider)
            Text(
                text = provider.label,
                color = AuthInk,
                fontFamily = AuthFont,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                letterSpacing = 0.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 24.dp)
            )
        }
    }
}

@Composable
fun AuthPrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthGreen,
            disabledContainerColor = Color(0xFFB8E8CC)
        ),
        shape = RoundedCornerShape(100.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = AuthFont,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.sp
        )
    }
}

@Composable
fun AuthOtpScreenContent(
    phone: String,
    code: String,
    onCodeChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onVerifyClick: () -> Unit,
    onResendClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AuthBackButton(onBackClick)

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))

        AuthTitle(
            title = "Enter OTP Code 🔐",
            subtitle = "Check your messages! We've sent a one-time code to $phone. Enter the code below to verify your account and continue."
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            repeat(6) { index ->
                OtpCell(
                    value = code.getOrNull(index)?.toString().orEmpty(),
                    active = index == code.length.coerceAtMost(5)
                )
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "You can resend the code in 56 seconds",
            color = AuthMuted,
            fontSize = 11.sp,
            fontFamily = AuthFont,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Resend code",
            color = AuthMuted,
            fontSize = 11.sp,
            fontFamily = AuthFont,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .clickable(onClick = onResendClick)
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(34.dp))

        AuthPrimaryButton(
            text = "Verify OTP",
            enabled = code.length == 6,
            onClick = onVerifyClick
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(30.dp))

        OtpKeypad(
            onDigit = { digit -> if (code.length < 6) onCodeChange(code + digit) },
            onBackspace = { if (code.isNotEmpty()) onCodeChange(code.dropLast(1)) }
        )
    }
}

@Composable
private fun OtpCell(
    value: String,
    active: Boolean
) {
    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 46.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(AuthField)
            .border(
                width = if (active) 1.5.dp else 0.dp,
                color = if (active) AuthGreen else Color.Transparent,
                shape = RoundedCornerShape(9.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            color = AuthInk,
            fontFamily = AuthFont,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun OtpKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("*", "0", "back")
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .size(width = 72.dp, height = 42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                when (key) {
                                    "back" -> onBackspace()
                                    "*" -> Unit
                                    else -> onDigit(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == "back") {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Backspace",
                                tint = AuthInk,
                                modifier = Modifier.size(17.dp)
                            )
                        } else {
                            Text(
                                text = key,
                                color = AuthInk,
                                fontFamily = AuthFont,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderIcon(provider: AuthProvider) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(provider.iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp)
        )
    }
}

enum class AuthProvider(
    val label: String,
    val iconRes: Int
) {
    Google("Continue with Google", R.drawable.ic_auth_google),
    Apple("Continue with Apple", R.drawable.ic_auth_apple),
    Facebook("Continue with Facebook", R.drawable.ic_auth_facebook),
    X("Continue with X", R.drawable.ic_auth_x)
}
