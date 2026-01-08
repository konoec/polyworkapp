package com.konoec.polyworkapp.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream

@Composable
actual fun rememberImagePicker(
    onFileSelected: (FilePickerResult?) -> Unit
): () -> Unit {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileName(context, it)
            val bytes = if (fileName.endsWith(".pdf", ignoreCase = true)) {
                uriToPdfByteArray(context, it)
            } else {
                uriToImageByteArray(context, it)
            }

            if (bytes != null) {
                onFileSelected(FilePickerResult(fileName, bytes))
            } else {
                onFileSelected(null)
            }
        } ?: onFileSelected(null)
    }

    // Acepta imágenes y PDFs
    return { launcher.launch("*/*") }
}

/**
 * Obtiene el nombre del archivo desde la URI
 */
private fun getFileName(context: Context, uri: Uri): String {
    var fileName = "archivo"

    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return fileName
}

/**
 * Convierte una URI de imagen a ByteArray con compresión
 */
private fun uriToImageByteArray(context: Context, uri: Uri): ByteArray? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Comprimir la imagen para reducir tamaño
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.toByteArray()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Convierte una URI de PDF a ByteArray
 */
private fun uriToPdfByteArray(context: Context, uri: Uri): ByteArray? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.readBytes()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

