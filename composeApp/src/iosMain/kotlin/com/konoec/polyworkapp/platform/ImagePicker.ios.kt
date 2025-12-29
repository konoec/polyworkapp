package com.konoec.polyworkapp.platform

import androidx.compose.runtime.Composable

@Composable
actual fun rememberImagePicker(
    onImageSelected: (ByteArray?) -> Unit
): () -> Unit {
    // TODO: Implementar picker de imágenes para iOS
    // Por ahora retorna una función vacía
    return {
        println("Image picker not implemented for iOS yet")
        onImageSelected(null)
    }
}

