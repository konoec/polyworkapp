package com.konoec.polyworkapp.platform

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.konoec.polyworkapp.data.local.appContext
import java.io.File

actual object PdfHandler {
    actual fun saveAndOpenPdf(bytes: ByteArray, fileName: String): Boolean {
        return try {
            val context = appContext
            val safeFileName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ — usar MediaStore para guardar en Downloads
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, safeFileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return false

                resolver.openOutputStream(uri)?.use { it.write(bytes) }

                // Abrir el PDF
                val openIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(openIntent)
            } else {
                // Android 9 y menor — guardar en cache y abrir con FileProvider
                val cacheDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
                val file = File(cacheDir, safeFileName)
                file.writeBytes(bytes)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val openIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(openIntent)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
