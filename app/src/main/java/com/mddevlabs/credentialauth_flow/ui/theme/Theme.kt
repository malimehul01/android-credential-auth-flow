package com.mddevlabs.credentialauth_flow.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary =DarkPrimary,
    onPrimary =DarkonPrimary,
    secondary = DarkSecondPrimary,
    onSecondary =DarkSecondonPrimary,
    surface = DarkSurface,
    tertiary =Darktertiary,
    background = Darkbackground
)

private val LightColorScheme = lightColorScheme(
    primary =LightPrimary,
    onPrimary = LightonPrimary,
    secondary = LightSecondprimary,
    onSecondary = LightSecondonprimary,
    surface = LightSurface,
    tertiary =Lighttertiary,
    background = Lightbackground
)

@Composable
fun CredentialAuthFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
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