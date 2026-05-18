package com.askara.photobooth.ui.theme

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object BrutalStyle {
    val CardShape = RoundedCornerShape(16.dp)
    val ButtonShape = RoundedCornerShape(8.dp)
    val BadgeShape = RoundedCornerShape(4.dp)

    val CardBorder = BorderStroke(4.dp, Slate950)
    val ButtonBorder = BorderStroke(2.dp, Slate950)
    val InputBorder = BorderStroke(2.dp, Slate950)

    val CardShadow: Dp = 8.dp
    val ButtonShadow: Dp = 4.dp
    val InputShadow: Dp = 2.dp
    val AppFrame: Dp = 8.dp
}

@Composable
fun BrutalCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = White,
    content: @Composable BoxScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 0.dp else BrutalStyle.CardShadow,
        animationSpec = tween(durationMillis = 100)
    )
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 0.dp,
        animationSpec = tween(durationMillis = 100)
    )

    Box(
        modifier = modifier
            .shadow(elevation, BrutalStyle.CardShape, ambientColor = BrutalShadow, spotColor = BrutalShadow)
            .then(
                Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            try {
                                awaitRelease()
                            } finally {
                                isPressed = false
                            }
                        },
                        onTap = { onClick() }
                    )
                }
            ),
        content = content
    )
}

@Composable
fun BrutalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Blue600,
    content: @Composable BoxScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 0.dp else BrutalStyle.ButtonShadow,
        animationSpec = tween(durationMillis = 100)
    )
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 0.dp,
        animationSpec = tween(durationMillis = 100)
    )

    Box(
        modifier = modifier
            .shadow(elevation, BrutalStyle.ButtonShape, ambientColor = BrutalShadow, spotColor = BrutalShadow)
            .then(
                Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            try {
                                awaitRelease()
                            } finally {
                                isPressed = false
                            }
                        },
                        onTap = { onClick() }
                    )
                }
            ),
        content = content
    )
}
