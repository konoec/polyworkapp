package com.konoec.polyworkapp.platform

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer as AndroidPdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.konoec.polyworkapp.data.local.appContext
import java.io.File

actual object PdfRenderer {
    actual fun renderFirstPage(pdfBytes: ByteArray, width: Int): ImageBitmap? {
        return try {
            // Escribir bytes a archivo temporal
            val tempFile = File(appContext.cacheDir, "preview_temp.pdf")
            tempFile.writeBytes(pdfBytes)

            val fd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = AndroidPdfRenderer(fd)
            val page = renderer.openPage(0)

            // Calcular dimensiones manteniendo aspect ratio
            val scale = width.toFloat() / page.width
            val bitmapWidth = width
            val bitmapHeight = (page.height * scale).toInt()

            val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.WHITE)

            page.render(bitmap, null, null, AndroidPdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            page.close()
            renderer.close()
            fd.close()
            tempFile.delete()

            bitmap.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
