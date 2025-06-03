package com.rix.womblab.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rix.womblab.presentation.theme.WombLabTheme
import com.rix.womblab.R

@Composable
fun LoadingIndicator(
    message: String = stringResource(id = R.string.loading_indicator_message),
    modifier: Modifier = Modifier,
    showMessage: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        )
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.home_emoji),
            fontSize = 32.sp,
            modifier = Modifier.rotate(rotation)
        )

        if (showMessage) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CompactLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Int = 24,
    strokeWidth: Int = 3
) {
    CircularProgressIndicator(
        modifier = modifier.size(size.dp),
        strokeWidth = strokeWidth.dp,
        color = MaterialTheme.colorScheme.primary
    )
}