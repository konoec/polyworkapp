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
    onFileSelected: (FilePickerResult) -> Unit
): () -> Unit {
    val context = LocalContext.current

    // Usar OpenDocument en lugar de GetContent para acceso completo al explorador
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) {
            onFileSelected(FilePickerResult.Cancelled)
            return@rememberLauncherForActivityResult
        }

        // Conceder permisos persistentes para la URI
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: Exception) {
            // Algunos proveedores no soportan permisos persistentes, continuar de todos modos
        }

        val fileName = getFileName(context, uri)
        val mimeType = context.contentResolver.getType(uri)

        // Validar que solo sea imagen o PDF
        val isValidFile = mimeType?.let { mime ->
            mime.startsWith("image/") || mime == "application/pdf"
        } ?: false

        if (!isValidFile) {
            onFileSelected(FilePickerResult.Error("Solo se permiten imágenes y archivos PDF"))
            return@rememberLauncherForActivityResult
        }

        // Validar tamaño del archivo (máximo 10MB)
        val fileSize = getFileSize(context, uri)
        if (fileSize > 10 * 1024 * 1024) { // 10MB
            val sizeMB = fileSize / (1024 * 1024)
            onFileSelected(FilePickerResult.Error("El archivo es muy grande (${sizeMB}MB). Máximo 10MB"))
            return@rememberLauncherForActivityResult
        }

        val bytes = if (fileName.endsWith(".pdf", ignoreCase = true) || mimeType == "application/pdf") {
            uriToPdfByteArray(context, uri)
        } else {
            uriToImageByteArray(context, uri)
        }

        if (bytes != null) {
            onFileSelected(FilePickerResult.Success(fileName, bytes))
        } else {
            onFileSelected(FilePickerResult.Error("No se pudo leer el archivo"))
        }
    }

    // Usar array de MIME types para mejor compatibilidad con el explorador
    return {
        launcher.launch(arrayOf("image/*", "application/pdf"))
    }
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
 * Obtiene el tamaño del archivo en bytes
 */
private fun getFileSize(context: Context, uri: Uri): Long {
    var fileSize = 0L

    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex >= 0 && cursor.moveToFirst()) {
                fileSize = cursor.getLong(sizeIndex)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return fileSize
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

