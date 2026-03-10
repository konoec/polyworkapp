package com.konoec.polyworkapp.platform

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Renderiza la primera página de un PDF como ImageBitmap para previsualizarlo.
 */
expect object PdfRenderer {
    fun renderFirstPage(pdfBytes: ByteArray, width: Int = 1000): ImageBitmap?
}
