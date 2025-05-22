package com.rix.womblab.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = WombLabPrimary,
    onPrimary = WombLabOnPrimary,
    primaryContainer = WombLabPrimaryLight,
    onPrimaryContainer = WombLabOnPrimary,

    secondary = WombLabSecondary,
    onSecondary = WombLabOnSecondary,
    secondaryContainer = WombLabSecondaryLight,
    onSecondaryContainer = WombLabOnSecondary,

    tertiary = WombLabAccent,
    onTertiary = WombLabOnPrimary,
    tertiaryContainer = WombLabAccentVariant,
    onTertiaryContainer = WombLabOnPrimary,

    error = WombLabError,
    onError = WombLabOnError,
    errorContainer = WombLabErrorContainer,
    onErrorContainer = WombLabOnErrorContainer,

    background = WombLabBackground,
    onBackground = WombLabOnBackground,
    surface = WombLabSurface,
    onSurface = WombLabOnSurface,
    surfaceVariant = WombLabSurfaceVariant,
    onSurfaceVariant = WombLabOnSurfaceVariant,

    outline = WombLabOnSurfaceVariant.copy(alpha = 0.5f),
    outlineVariant = WombLabOnSurfaceVariant.copy(alpha = 0.3f)
)

private val DarkColorScheme = darkColorScheme(
    primary = WombLabPrimaryDark,
    onPrimary = WombLabOnPrimary,
    primaryContainer = WombLabPrimaryVariantDark,
    onPrimaryContainer = WombLabOnPrimary,

    secondary = WombLabSecondaryDark,
    onSecondary = WombLabOnSecondary,
    secondaryContainer = WombLabSecondaryVariant,
    onSecondaryContainer = WombLabOnSecondary,

    tertiary = WombLabAccent,
    onTertiary = WombLabOnPrimary,
    tertiaryContainer = WombLabAccentVariant,
    onTertiaryContainer = WombLabOnPrimary,

    error = WombLabError,
    onError = WombLabOnError,
    errorContainer = WombLabErrorContainer,
    onErrorContainer = WombLabOnErrorContainer,

    background = WombLabBackgroundDark,
    onBackground = WombLabOnSurfaceDark,
    surface = WombLabSurfaceDark,
    onSurface = WombLabOnSurfaceDark,
    surfaceVariant = WombLabSurfaceVariantDark,
    onSurfaceVariant = WombLabOnSurfaceVariantDark,

    outline = WombLabOnSurfaceVariantDark.copy(alpha = 0.5f),
    outlineVariant = WombLabOnSurfaceVariantDark.copy(alpha = 0.3f)
)

@Composable
fun WombLabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    materialYou: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        materialYou && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun successColors() = MaterialTheme.colorScheme.run {
    if (isSystemInDarkTheme()) {
        Pair(WombLabSuccess, WombLabOnSuccess)
    } else {
        Pair(WombLabSuccess, WombLabOnSuccess)
    }
}

@Composable
fun warningColors() = MaterialTheme.colorScheme.run {
    if (isSystemInDarkTheme()) {
        Pair(WombLabWarning, WombLabOnWarning)
    } else {
        Pair(WombLabWarning, WombLabOnWarning)
    }
}

@Composable
fun infoColors() = MaterialTheme.colorScheme.run {
    if (isSystemInDarkTheme()) {
        Pair(WombLabInfo, WombLabOnInfo)
    } else {
        Pair(WombLabInfo, WombLabOnInfo)
    }
}