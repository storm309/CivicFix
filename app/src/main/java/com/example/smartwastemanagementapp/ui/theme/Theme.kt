package com.example.smartwastemanagementapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Light Scheme ────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary                = EcoGreen40,
    onPrimary              = Color.White,
    primaryContainer       = EcoGreen90,
    onPrimaryContainer     = EcoGreen10,
    secondary              = Teal40,
    onSecondary            = Color.White,
    secondaryContainer     = Teal90,
    onSecondaryContainer   = Teal10,
    tertiary               = Amber40,
    onTertiary             = Color.White,
    tertiaryContainer      = Amber90,
    onTertiaryContainer    = Amber10,
    error                  = Color(0xFFFF3B30),
    onError                = Color.White,
    errorContainer         = Color(0xFFFFDAD6),
    onErrorContainer       = Color(0xFF410002),
    background             = Color(0xFFF7F8FA),
    onBackground           = Color(0xFF111827),
    surface                = Color(0xFFFFFFFF),
    onSurface              = Color(0xFF111827),
    surfaceVariant         = Color(0xFFF1F4F8),
    onSurfaceVariant       = Color(0xFF6B7280),
    outline                = Color(0xFFC9D1DC),
    outlineVariant         = NeutralVar80,
    scrim                  = Color(0xFF000000),
    inverseSurface         = Color(0xFF2D3130),
    inverseOnSurface       = Color(0xFFEEF1ED),
    inversePrimary         = EcoGreen80,
)

// ── Dark Scheme ─────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary                = EcoGreen80,
    onPrimary              = EcoGreen20,
    primaryContainer       = EcoGreen30,
    onPrimaryContainer     = EcoGreen90,
    secondary              = Teal80,
    onSecondary            = Teal20,
    secondaryContainer     = Teal40,
    onSecondaryContainer   = Teal90,
    tertiary               = Amber80,
    onTertiary             = Amber10,
    tertiaryContainer      = Color(0xFF6A3C00),
    onTertiaryContainer    = Amber90,
    error                  = Color(0xFFFFB4AB),
    onError                = Color(0xFF690005),
    errorContainer         = Color(0xFF93000A),
    onErrorContainer       = Color(0xFFFFDAD6),
    background             = Color(0xFF10131A),
    onBackground           = Color(0xFFE7EAF0),
    surface                = Color(0xFF161B23),
    onSurface              = Color(0xFFE7EAF0),
    surfaceVariant         = NeutralVar40,
    onSurfaceVariant       = NeutralVar80,
    outline                = Color(0xFF8A9490),
    outlineVariant         = NeutralVar40,
    inverseSurface         = Color(0xFFE1E3DF),
    inverseOnSurface       = Color(0xFF2D3130),
    inversePrimary         = EcoGreen40,
)

@Composable
fun SmartWasteManagementAppTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}