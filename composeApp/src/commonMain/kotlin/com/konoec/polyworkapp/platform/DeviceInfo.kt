package com.konoec.polyworkapp.platform

/**
 * Obtiene información del dispositivo específica de cada plataforma
 */
expect object DeviceInfo {
    fun getDeviceId(): String
}

