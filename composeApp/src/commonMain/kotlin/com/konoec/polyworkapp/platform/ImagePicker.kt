package com.konoec.polyworkapp.platform

import androidx.compose.runtime.Composable

/**
 * Datos del archivo seleccionado
 */
data class FilePickerResult(
    val fileName: String,
    val bytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FilePickerResult

        if (fileName != other.fileName) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

/**
 * Función composable para registrar un selector de archivos (imágenes y PDF) específico de cada plataforma
 */
@Composable
expect fun rememberImagePicker(
    onFileSelected: (FilePickerResult?) -> Unit
): () -> Unit

