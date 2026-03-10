package com.konoec.polyworkapp.platform

import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.writeToFile
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.UIKit.UIActivityViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned

actual object PdfHandler {
    @OptIn(ExperimentalForeignApi::class)
    actual fun saveAndOpenPdf(bytes: ByteArray, fileName: String): Boolean {
        return try {
            val tempDir = NSTemporaryDirectory()
            val filePath = "$tempDir$fileName"

            val nsData = bytes.usePinned { pinned ->
                NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
            }
            nsData.writeToFile(filePath, atomically = true)

            val fileUrl = NSURL.fileURLWithPath(filePath)
            val activityVC = UIActivityViewController(
                activityItems = listOf(fileUrl),
                applicationActivities = null
            )

            val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
            rootVC?.presentViewController(activityVC, animated = true, completion = null)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
