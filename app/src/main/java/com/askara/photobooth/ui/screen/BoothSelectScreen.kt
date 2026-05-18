package com.askara.photobooth.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askara.photobooth.data.model.Booth
import com.askara.photobooth.ui.theme.Blue600
import com.askara.photobooth.ui.theme.BrutalStyle
import com.askara.photobooth.ui.theme.Emerald100
import com.askara.photobooth.ui.theme.Emerald300
import com.askara.photobooth.ui.theme.Emerald700
import com.askara.photobooth.ui.theme.Red500
import com.askara.photobooth.ui.theme.Slate500
import com.askara.photobooth.ui.theme.Slate950
import com.askara.photobooth.ui.theme.White
import com.askara.photobooth.ui.theme.Yellow400
import com.askara.photobooth.viewmodel.AuthViewModel
import com.askara.photobooth.viewmodel.BoothSelectViewModel

@Composable
fun BoothSelectScreen(
    viewModel: BoothSelectViewModel,
    authViewModel: AuthViewModel,
    tenantId: String,
    onBoothSelected: (Booth) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
    val statusBarPadding = if (isPortrait) {
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    } else {
        0.dp
    }

    LaunchedEffect(tenantId) {
        viewModel.loadBooths(tenantId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 30.dp + statusBarPadding, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SELECT BOOTH",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.3).sp,
                    color = Slate950
                )
                Text(
                    text = "CHOOSE YOUR BOOTH TO START",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Slate500
                )
            }
            Surface(
                onClick = {
                    authViewModel.signOut()
                    onLogout()
                },
                shape = BrutalStyle.ButtonShape,
                color = Red500,
                border = BrutalStyle.ButtonBorder,
                shadowElevation = BrutalStyle.ButtonShadow
            ) {
                Text(
                    text = "LOGOUT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Blue600)
            }
        } else if (uiState.booths.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NO BOOTHS",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.3).sp,
                        color = Slate950
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "NO BOOTHS AVAILABLE YET",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = Slate500
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(uiState.booths) { booth ->
                    val isOnline = booth.status == "online"
                    val isInSession = booth.status == "in_session"

                    Surface(
                        onClick = { onBoothSelected(booth) },
                        shape = BrutalStyle.CardShape,
                        color = White,
                        border = BrutalStyle.CardBorder,
                        shadowElevation = BrutalStyle.CardShadow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = booth.name.uppercase(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.3).sp,
                                color = Slate950
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = BrutalStyle.BadgeShape,
                                color = when {
                                    isOnline -> Emerald100
                                    isInSession -> Yellow400.copy(alpha = 0.2f)
                                    else -> Slate500.copy(alpha = 0.1f)
                                },
                                border = BorderStroke(2.dp, when {
                                    isOnline -> Emerald300
                                    isInSession -> Yellow400
                                    else -> Slate500
                                })
                            ) {
                                Text(
                                    text = booth.status.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = when {
                                        isOnline -> Emerald700
                                        isInSession -> Slate950
                                        else -> Slate500
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
