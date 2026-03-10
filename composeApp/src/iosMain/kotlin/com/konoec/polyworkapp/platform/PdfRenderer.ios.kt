package com.konoec.polyworkapp.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGContextDrawPDFPage
import platform.CoreGraphics.CGContextScaleCTM
import platform.CoreGraphics.CGContextSetRGBFillColor
import platform.CoreGraphics.CGContextFillRect
import platform.CoreGraphics.CGPDFDocumentCreateWithProvider
import platform.CoreGraphics.CGPDFDocumentGetPage
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.kCGImageAlphaPremultipliedLast
import platform.Foundation.NSData
import platform.Foundation.create
import platform.CoreGraphics.CGDataProviderCreateWithCFData

actual object PdfRenderer {
    @OptIn(ExperimentalForeignApi::class)
    actual fun renderFirstPage(pdfBytes: ByteArray, width: Int): ImageBitmap? {
        return try {
            val nsData = pdfBytes.usePinned { pinned ->
                NSData.create(bytes = pinned.addressOf(0), length = pdfBytes.size.toULong())
            }

            val provider = CGDataProviderCreateWithCFData(nsData) ?: return null
            val document = CGPDFDocumentCreateWithProvider(provider) ?: return null
            val page = CGPDFDocumentGetPage(document, 1) ?: return null

            val pageRect = platform.CoreGraphics.CGPDFPageGetBoxRect(page, platform.CoreGraphics.kCGPDFMediaBox)
            val scale = width.toDouble() / pageRect.size.width
            val bitmapWidth = width
            val bitmapHeight = (pageRect.size.height * scale).toInt()

            val colorSpace = CGColorSpaceCreateDeviceRGB()
            val context = CGBitmapContextCreate(
                data = null,
                width = bitmapWidth.toULong(),
                height = bitmapHeight.toULong(),
                bitsPerComponent = 8u,
                bytesPerRow = (bitmapWidth * 4).toULong(),
                space = colorSpace,
                bitmapInfo = kCGImageAlphaPremultipliedLast
            ) ?: return null

            // Fondo blanco
            CGContextSetRGBFillColor(context, 1.0, 1.0, 1.0, 1.0)
            CGContextFillRect(context, CGRectMake(0.0, 0.0, bitmapWidth.toDouble(), bitmapHeight.toDouble()))

            CGContextScaleCTM(context, scale, scale)
            CGContextDrawPDFPage(context, page)

            val cgImage = CGBitmapContextCreateImage(context) ?: return null

            // Convertir a Skia Image → ImageBitmap
            // Para iOS usamos un enfoque alternativo: renderizar con UIGraphics
            null // TODO: Implementar conversión CGImage → ImageBitmap en iOS
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
