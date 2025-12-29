package com.konoec.polyworkapp

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import com.konoec.polyworkapp.presentation.navigation.MainNavigation
import com.konoec.polyworkapp.presentation.theme.PolyworkAppTheme
import com.konoec.polyworkapp.presentation.theme.windowClassForWidthDp

@Composable
fun App() {
    BoxWithConstraints {
        val wc = windowClassForWidthDp(maxWidth.value.toInt())
        PolyworkAppTheme(windowClass = wc) {
            MainNavigation()
        }
    }
}