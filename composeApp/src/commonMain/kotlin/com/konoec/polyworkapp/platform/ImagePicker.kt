package com.konoec.polyworkapp.platform

import androidx.compose.runtime.Composable

/**
 * Función composable para registrar un selector de imágenes específico de cada plataforma
 */
@Composable
expect fun rememberImagePicker(
    onImageSelected: (ByteArray?) -> Unit
): () -> Unit

