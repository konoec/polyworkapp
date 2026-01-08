package com.konoec.polyworkapp.platform

import androidx.compose.runtime.Composable

@Composable
actual fun rememberImagePicker(
    onFileSelected: (FilePickerResult) -> Unit
): () -> Unit {
    // TODO: Implementar picker de archivos (imágenes y PDF) para iOS
    // Por ahora retorna una función vacía
    return {
        println("File picker not implemented for iOS yet")
        onFileSelected(FilePickerResult.Error("Selector de archivos no implementado en iOS"))
    }
}
