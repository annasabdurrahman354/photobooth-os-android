package com.askara.photobooth.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askara.photobooth.ui.theme.Blue600
import com.askara.photobooth.ui.theme.BrutalShadow
import com.askara.photobooth.ui.theme.BrutalStyle
import com.askara.photobooth.ui.theme.Red500
import com.askara.photobooth.ui.theme.Slate500
import com.askara.photobooth.ui.theme.Slate800
import com.askara.photobooth.ui.theme.Slate950
import com.askara.photobooth.ui.theme.White
import com.askara.photobooth.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    if (uiState.isSignedIn && uiState.profile != null) {
        onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "PHOTOBOOTH",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp,
            color = Slate950
        )
        Text(
            text = "OS",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            color = Blue600
        )

        Spacer(modifier = Modifier.height(48.dp))

        Surface(
            shape = BrutalStyle.CardShape,
            color = White,
            border = BrutalStyle.CardBorder,
            shadowElevation = BrutalStyle.CardShadow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "SIGN IN",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Slate800
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "EMAIL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Slate500,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(BrutalStyle.InputShadow, BrutalStyle.ButtonShape, ambientColor = BrutalShadow, spotColor = BrutalShadow),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = BrutalStyle.ButtonShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate950,
                        unfocusedTextColor = Slate950,
                        focusedBorderColor = Slate950,
                        unfocusedBorderColor = Slate950,
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        cursorColor = Slate950
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PASSWORD",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Slate500,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(BrutalStyle.InputShadow, BrutalStyle.ButtonShape, ambientColor = BrutalShadow, spotColor = BrutalShadow),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = BrutalStyle.ButtonShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate950,
                        unfocusedTextColor = Slate950,
                        focusedBorderColor = Slate950,
                        unfocusedBorderColor = Slate950,
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        cursorColor = Slate950
                    )
                )

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.error!!,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Red500
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            authViewModel.signIn(email, password)
                        }
                    },
                    shape = BrutalStyle.ButtonShape,
                    color = Blue600,
                    border = BrutalStyle.ButtonBorder,
                    shadowElevation = BrutalStyle.ButtonShadow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "LOGIN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = White
                            )
                        }
                    }
                }
            }
        }
    }
}
