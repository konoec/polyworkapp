package com.konoec.polyworkapp.platform

/**
 * Guarda un PDF en el almacenamiento del dispositivo y lo abre con el visor predeterminado.
 * Retorna true si se guardó/abrió correctamente.
 */
expect object PdfHandler {
    fun saveAndOpenPdf(bytes: ByteArray, fileName: String): Boolean
}
