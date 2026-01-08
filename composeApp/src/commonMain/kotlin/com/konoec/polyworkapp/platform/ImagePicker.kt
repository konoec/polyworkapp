package com.konoec.polyworkapp.platform

import androidx.compose.runtime.Composable

/**
 * Resultado de la selección de archivo
 */
sealed class FilePickerResult {
    data class Success(
        val fileName: String,
        val bytes: ByteArray
    ) : FilePickerResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Success

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

    data class Error(val message: String) : FilePickerResult()
    object Cancelled : FilePickerResult()
}

/**
 * Función composable para registrar un selector de archivos (imágenes y PDF) específico de cada plataforma
 */
@Composable
expect fun rememberImagePicker(
    onFileSelected: (FilePickerResult) -> Unit
): () -> Unit
