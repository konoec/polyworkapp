package com.konoec.polyworkapp.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
enum class PolyworkWindowClass {
    Compact,
    Medium,
    Expanded,
}

fun windowClassForWidthDp(widthDp: Int): PolyworkWindowClass = when {
    widthDp < 600 -> PolyworkWindowClass.Compact
    widthDp < 840 -> PolyworkWindowClass.Medium
    else -> PolyworkWindowClass.Expanded
}

@Immutable
data class PolyworkDimens(
    val screenPadding: Dp,
    val cardPadding: Dp,
    val gap: Dp,
    val heroHeight: Dp,
    val maxContentWidth: Dp,
)

fun dimensFor(windowClass: PolyworkWindowClass): PolyworkDimens = when (windowClass) {
    PolyworkWindowClass.Compact -> PolyworkDimens(
        screenPadding = 16.dp,
        cardPadding = 12.dp,
        gap = 12.dp,
        heroHeight = 120.dp,
        maxContentWidth = 520.dp,
    )

    PolyworkWindowClass.Medium -> PolyworkDimens(
        screenPadding = 20.dp,
        cardPadding = 14.dp,
        gap = 16.dp,
        heroHeight = 140.dp,
        maxContentWidth = 720.dp,
    )

    PolyworkWindowClass.Expanded -> PolyworkDimens(
        screenPadding = 24.dp,
        cardPadding = 16.dp,
        gap = 18.dp,
        heroHeight = 160.dp,
        maxContentWidth = 840.dp,
    )
}
