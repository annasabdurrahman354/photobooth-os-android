package com.askara.photobooth.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.askara.photobooth.BuildConfig
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
import com.askara.photobooth.viewmodel.SessionState
import com.askara.photobooth.viewmodel.SessionViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun SessionScreen(
    viewModel: SessionViewModel,
    boothId: String,
    templateId: String,
    totalSlots: Int,
    onDone: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.initSession(boothId, templateId, totalSlots)
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val uiState by viewModel.uiState.collectAsState()

    when (uiState.currentState) {
        SessionState.IDLE -> IdleView(viewModel = viewModel)
        SessionState.SHOOTING -> ShootingView(
            viewModel = viewModel,
            hasCameraPermission = hasCameraPermission,
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) }
        )
        SessionState.REVIEW -> ReviewView(
            viewModel = viewModel,
            onRender = {
                val sessionId = viewModel.uiState.value.session?.id
                if (sessionId != null) {
                    viewModel.confirmAndUpload {
                        viewModel.setState(SessionState.RENDERING)
                    }
                }
            }
        )
        SessionState.RENDERING -> RenderingView(
            viewModel = viewModel,
            sessionId = uiState.session?.id ?: ""
        )
        SessionState.DONE -> DoneView(viewModel = viewModel, onDone = onDone)
    }
}

@Composable
private fun IdleView(viewModel: SessionViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "READY TO START?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.3).sp,
                color = Slate950
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${uiState.totalSlots} PHOTO${if (uiState.totalSlots > 1) "S" else ""} WILL BE TAKEN",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = Slate500
            )
            Spacer(modifier = Modifier.height(32.dp))
            Surface(
                onClick = { viewModel.startShooting() },
                shape = BrutalStyle.CardShape,
                color = Yellow400,
                border = BrutalStyle.CardBorder,
                shadowElevation = BrutalStyle.CardShadow
            ) {
                Text(
                    text = "TOUCH TO START",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Slate950,
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 32.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ShootingView(
    viewModel: SessionViewModel,
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) onRequestPermission()
    }

    if (!hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "CAMERA PERMISSION REQUIRED",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Slate950
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    onClick = onRequestPermission,
                    shape = BrutalStyle.ButtonShape,
                    color = Yellow400,
                    border = BrutalStyle.ButtonBorder,
                    shadowElevation = BrutalStyle.ButtonShadow
                ) {
                    Text(
                        text = "GRANT PERMISSION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        color = Slate950
                    )
                }
            }
        }
        return
    }

    val cameraController = remember { LifecycleCameraController(context) }
    var countdown by remember { mutableIntStateOf(0) }
    var isFlashing by remember { mutableStateOf(false) }

    LaunchedEffect(cameraController) {
        cameraController.bindToLifecycle(lifecycleOwner)
    }

    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000L)
            countdown -= 1
            if (countdown == 0) {
                isFlashing = true
                cameraController.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val buffer = image.planes[0].buffer
                            val bytes = ByteArray(buffer.remaining())
                            buffer.get(bytes)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            image.close()
                            viewModel.addCapture(bitmap)
                        }

                        override fun onError(exception: androidx.camera.core.ImageCaptureException) {
                            super.onError(exception)
                            isFlashing = false
                        }
                    }
                )
            }
        }
    }

    LaunchedEffect(isFlashing) {
        if (isFlashing) {
            delay(150L)
            isFlashing = false
        }
    }

    val allCaptured = uiState.captures.size >= uiState.totalSlots
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
    val statusBarPadding = if (isPortrait) {
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    } else {
        0.dp
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    this.controller = cameraController
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Countdown Overlay
        if (countdown > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = countdown.toString(),
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Black,
                    color = White
                )
            }
        }

        // Flash Effect
        if (isFlashing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        }

        Surface(
            shape = BrutalStyle.ButtonShape,
            color = Slate950,
            border = BrutalStyle.ButtonBorder,
            shadowElevation = BrutalStyle.ButtonShadow,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp + statusBarPadding)
        ) {
            Text(
                text = "${uiState.captures.size} / ${uiState.totalSlots}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!allCaptured) {
                Surface(
                    onClick = {
                        if (countdown == 0) {
                            countdown = 3
                        }
                    },
                    shape = BrutalStyle.CardShape,
                    color = if (countdown > 0) Slate500 else Yellow400,
                    border = BrutalStyle.CardBorder,
                    shadowElevation = if (countdown > 0) 0.dp else BrutalStyle.CardShadow,
                    enabled = countdown == 0
                ) {
                    Text(
                        text = if (countdown > 0) "GET READY..." else "TAKE PHOTO ${uiState.captures.size + 1}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = if (countdown > 0) White else Slate950,
                        modifier = Modifier.padding(horizontal = 48.dp, vertical = 16.dp)
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        onClick = { viewModel.goToReview() },
                        shape = BrutalStyle.ButtonShape,
                        color = Emerald100,
                        border = BorderStroke(2.dp, Emerald300),
                        shadowElevation = BrutalStyle.ButtonShadow
                    ) {
                        Text(
                            text = "REVIEW PHOTOS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = Emerald700,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }
                    Surface(
                        onClick = { viewModel.retakeAll() },
                        shape = BrutalStyle.ButtonShape,
                        color = Red500,
                        border = BrutalStyle.ButtonBorder,
                        shadowElevation = BrutalStyle.ButtonShadow
                    ) {
                        Text(
                            text = "RETAKE ALL",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = White,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewView(viewModel: SessionViewModel, onRender: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
    val statusBarPadding = if (isPortrait) {
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    } else {
        0.dp
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 30.dp + statusBarPadding, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "REVIEW YOUR PHOTOS",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.3).sp,
                color = Slate950
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${uiState.captures.size} PHOTOS CAPTURED",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = Slate500
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(uiState.captures) { index, bytes ->
                    val bitmap = remember(bytes) {
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                    Surface(
                        onClick = { viewModel.selectPreview(index) },
                        shape = BrutalStyle.CardShape,
                        color = White,
                        border = BrutalStyle.CardBorder,
                        shadowElevation = BrutalStyle.ButtonShadow
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Capture $index",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.error != null) {
                Surface(
                    shape = BrutalStyle.CardShape,
                    color = Color.White,
                    border = BorderStroke(2.dp, Red500),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "ERROR: ${uiState.error}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Red500,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Surface(
                onClick = { if (!uiState.isLoading) onRender() },
                shape = BrutalStyle.ButtonShape,
                color = if (uiState.isLoading) Emerald100.copy(alpha = 0.5f) else Emerald100,
                border = BorderStroke(2.dp, if (uiState.isLoading) Emerald300.copy(alpha = 0.5f) else Emerald300),
                shadowElevation = if (uiState.isLoading) 0.dp else BrutalStyle.ButtonShadow,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 14.dp, horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Emerald700,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = if (uiState.isLoading) "UPLOADING..." else "LOOKS GREAT!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = if (uiState.isLoading) Emerald700.copy(alpha = 0.5f) else Emerald700,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                onClick = { viewModel.retakeAll() },
                shape = BrutalStyle.ButtonShape,
                color = Red500,
                border = BrutalStyle.ButtonBorder,
                shadowElevation = BrutalStyle.ButtonShadow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "RETAKE ALL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = White,
                    modifier = Modifier.padding(vertical = 14.dp, horizontal = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Full Screen Preview Overlay
        if (uiState.selectedPreviewIndex != null) {
            val index = uiState.selectedPreviewIndex!!
            val bytes = uiState.captures[index]
            val bitmap = remember(bytes) {
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Slate950.copy(alpha = 0.9f))
                    .clickable(enabled = true, onClick = { viewModel.selectPreview(null) }),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Full Preview",
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat())
                        .clickable(enabled = false) {}, // Prevent click-through to close
                    contentScale = ContentScale.Fit
                )

                Surface(
                    onClick = { viewModel.selectPreview(null) },
                    shape = BrutalStyle.ButtonShape,
                    color = Red500,
                    border = BrutalStyle.ButtonBorder,
                    shadowElevation = BrutalStyle.ButtonShadow,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "X",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderingView(viewModel: SessionViewModel, sessionId: String) {
    val uiState by viewModel.uiState.collectAsState()
    val error = uiState.error

    Box(modifier = Modifier.fillMaxSize()) {
        if (error == null) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowContentAccess = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true

                        val mainHandler = Handler(Looper.getMainLooper())

                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onRenderComplete(finalImageUrl: String, shareToken: String) {
                                mainHandler.post { viewModel.onRenderComplete(finalImageUrl) }
                            }

                            @JavascriptInterface
                            fun onRenderError(errorMessage: String) {
                                mainHandler.post { viewModel.onRenderError(errorMessage) }
                            }
                        }, "Android")

                        webViewClient = object : WebViewClient() {
                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame == true) {
                                    viewModel.onRenderError(
                                        error?.description?.toString() ?: "Failed to load page"
                                    )
                                }
                            }
                        }

                        val token = viewModel.getAccessToken() ?: ""
                        loadUrl("${BuildConfig.WEB_APP_URL}/render/$sessionId?token=$token")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "RENDER ERROR",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.3).sp,
                    color = Red500
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = BrutalStyle.CardShape,
                    color = White,
                    border = BrutalStyle.CardBorder,
                    shadowElevation = BrutalStyle.ButtonShadow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate950,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    onClick = {
                        viewModel.clearError()
                    },
                    shape = BrutalStyle.ButtonShape,
                    color = Yellow400,
                    border = BrutalStyle.CardBorder,
                    shadowElevation = BrutalStyle.ButtonShadow
                ) {
                    Text(
                        text = "RETRY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = Slate950,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    onClick = {
                        viewModel.clearError()
                        viewModel.setState(SessionState.REVIEW)
                    },
                    shape = BrutalStyle.ButtonShape,
                    color = Red500,
                    border = BrutalStyle.ButtonBorder,
                    shadowElevation = BrutalStyle.ButtonShadow
                ) {
                    Text(
                        text = "BACK TO REVIEW",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = White,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DoneView(viewModel: SessionViewModel, onDone: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val shareToken = uiState.session?.share_token
    val shareUrl = if (shareToken != null) "${BuildConfig.WEB_APP_URL}/share/$shareToken" else null
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
    val statusBarPadding = if (isPortrait) {
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    } else {
        0.dp
    }

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(shareUrl) {
        if (shareUrl != null) {
            qrBitmap = withContext(Dispatchers.Default) { generateQrBitmap(shareUrl, 400) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp + statusBarPadding, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ALL DONE!",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp,
            color = Slate950
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "YOUR PHOTOS ARE READY",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            color = Slate500
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.finalImageUrl != null) {
            Surface(
                shape = BrutalStyle.CardShape,
                color = White,
                border = BrutalStyle.CardBorder,
                shadowElevation = BrutalStyle.CardShadow,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = uiState.finalImageUrl,
                    contentDescription = "Final Result",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (uiState.captures.isNotEmpty()) {
            Text(
                text = "CAPTURED PHOTOS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = Slate500
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                uiState.captures.forEachIndexed { index, bytes ->
                    val bitmap = remember(bytes) {
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                    Surface(
                        shape = BrutalStyle.CardShape,
                        color = White,
                        border = BrutalStyle.CardBorder,
                        shadowElevation = BrutalStyle.ButtonShadow
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Capture $index",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (shareUrl != null && qrBitmap != null) {
            Text(
                text = "SCAN TO DOWNLOAD",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = Slate500
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = BrutalStyle.CardShape,
                color = White,
                border = BrutalStyle.CardBorder,
                shadowElevation = BrutalStyle.CardShadow
            ) {
                Image(
                    bitmap = qrBitmap!!.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(12.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = BrutalStyle.ButtonShape,
                color = White,
                border = BrutalStyle.ButtonBorder,
                shadowElevation = BrutalStyle.ButtonShadow
            ) {
                Text(
                    text = shareUrl,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Blue600,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            onClick = {
                viewModel.resetSession()
                onDone()
            },
            shape = BrutalStyle.ButtonShape,
            color = Blue600,
            border = BrutalStyle.ButtonBorder,
            shadowElevation = BrutalStyle.ButtonShadow
        ) {
            Text(
                text = "DONE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = White,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 14.dp)
            )
        }
    }
}

private fun generateQrBitmap(content: String, size: Int): Bitmap {
    val hints = mapOf(EncodeHintType.MARGIN to 1)
    val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
    val pixels = IntArray(size * size)
    for (y in 0 until size) {
        for (x in 0 until size) {
            pixels[y * size + x] = if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
        }
    }
    return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
}
