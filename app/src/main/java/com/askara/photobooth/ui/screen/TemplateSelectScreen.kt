package com.askara.photobooth.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.askara.photobooth.data.model.Template
import com.askara.photobooth.ui.theme.Blue600
import com.askara.photobooth.ui.theme.BrutalStyle
import com.askara.photobooth.ui.theme.Slate500
import com.askara.photobooth.ui.theme.Slate950
import com.askara.photobooth.ui.theme.White
import com.askara.photobooth.viewmodel.TemplateSelectViewModel

@Composable
fun TemplateSelectScreen(
    viewModel: TemplateSelectViewModel,
    tenantId: String,
    boothId: String,
    onTemplateSelected: (Template) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
    val statusBarPadding = if (isPortrait) {
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    } else {
        0.dp
    }

    LaunchedEffect(tenantId, boothId) {
        viewModel.loadData(tenantId, boothId)
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
                    text = "SELECT TEMPLATE",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.3).sp,
                    color = Slate950
                )
                Text(
                    text = "BOOTH: ${uiState.booth?.name?.uppercase() ?: "LOADING..."}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Slate500
                )
            }
            Surface(
                onClick = onBack,
                shape = BrutalStyle.ButtonShape,
                color = Slate950,
                border = BrutalStyle.ButtonBorder,
                shadowElevation = BrutalStyle.ButtonShadow
            ) {
                Text(
                    text = "BACK",
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
        } else if (uiState.templates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NO TEMPLATES",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.3).sp,
                        color = Slate950
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "THIS BOOTH HAS NO TEMPLATES YET",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = Slate500
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (isPortrait) 2 else 3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.templates) { template ->
                    Surface(
                        onClick = { onTemplateSelected(template) },
                        shape = BrutalStyle.CardShape,
                        color = White,
                        border = BrutalStyle.CardBorder,
                        shadowElevation = BrutalStyle.CardShadow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(if (isPortrait) 0.8f else 1.2f)
                                    .background(Slate950.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (template.thumbnail_url != null) {
                                    AsyncImage(
                                        model = template.thumbnail_url,
                                        contentDescription = template.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = "NO PREVIEW",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Slate500
                                    )
                                }
                            }
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = template.name.uppercase(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.3).sp,
                                    color = Slate950,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
