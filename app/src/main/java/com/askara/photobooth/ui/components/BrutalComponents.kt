package com.askara.photobooth.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.askara.photobooth.ui.theme.Slate950

@Composable
fun BrutalCard(
    modifier: Modifier = Modifier,
    shadowOffset: Dp = 4.dp,
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.shadow(shadowOffset, RoundedCornerShape(cornerRadius), ambientColor = Color.Black, spotColor = Color.Black),
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor,
        border = BorderStroke(2.dp, Slate950),
        content = content
    )
}

@Composable
fun BrutalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = Color.White,
    textColor: Color = Slate950,
    shadowOffset: Dp = 4.dp,
    cornerRadius: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor,
        border = BorderStroke(2.dp, Slate950),
        shadowElevation = if (enabled) shadowOffset else 0.dp,
        content = content
    )
}