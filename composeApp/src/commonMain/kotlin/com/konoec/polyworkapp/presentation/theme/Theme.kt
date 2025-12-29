package com.konoec.polyworkapp.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color


private val PolyworkColorScheme = darkColorScheme(
    primary = PolyRed,
    onPrimary = PolyWhite,
    primaryContainer = PolyRedDark,
    onPrimaryContainer = Color(0xFFFFD9E2),

    secondary = PolySecondary,
    onSecondary = PolyOnSecondary,
    secondaryContainer = PolySecondaryContainer,
    onSecondaryContainer = PolyWhite,

    tertiary = PolyRed,
    onTertiary = PolyWhite,

    background = PolyBackground,
    onBackground = PolyWhite,
    surface = PolySurface,
    onSurface = PolyWhite,

    surfaceVariant = PolySurfaceBorder,
    onSurfaceVariant = PolyGray,

    outline = PolyOutline,
    outlineVariant = PolyOutlineVariant,

    error = PolyError,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    inverseSurface = PolyInverseSurface,
    inverseOnSurface = PolyInverseOnSurface,
    inversePrimary = PolyInversePrimary,
)

private val LocalPolyworkWindowClass = staticCompositionLocalOf { PolyworkWindowClass.Compact }
private val LocalPolyworkDimens = staticCompositionLocalOf { dimensFor(PolyworkWindowClass.Compact) }

val PolyworkTheme: PolyworkThemeAccessor
    @Composable get() = PolyworkThemeAccessor

object PolyworkThemeAccessor {
    val windowClass: PolyworkWindowClass
        @Composable get() = LocalPolyworkWindowClass.current

    val dimens: PolyworkDimens
        @Composable get() = LocalPolyworkDimens.current
}

@Composable
fun PolyworkAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    windowClass: PolyworkWindowClass = PolyworkWindowClass.Compact,
    content: @Composable () -> Unit
) {
    val dimens = dimensFor(windowClass)

    CompositionLocalProvider(
        LocalPolyworkWindowClass provides windowClass,
        LocalPolyworkDimens provides dimens
    ) {
        MaterialTheme(
            colorScheme = PolyworkColorScheme,
            typography = PolyTypography,
            shapes = PolyShapes,
            content = content
        )
    }
}
