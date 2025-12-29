package com.konoec.polyworkapp.data.remote

object ApiConfig {
    // Cambiar esta URL según el entorno
    // Para desarrollo local: "http://10.0.2.2:8080" (Android Emulator)
    // Para dispositivo físico: "http://192.168.0.96:8080"
    // Para producción: "https://api.tudominio.com"
    const val BASE_URL = "http://10.98.254.2:8080/polybags/rest/workapp"

    // Timeouts en milisegundos
    const val CONNECT_TIMEOUT = 30_000L
    const val READ_TIMEOUT = 30_000L
    const val WRITE_TIMEOUT = 30_000L
}

