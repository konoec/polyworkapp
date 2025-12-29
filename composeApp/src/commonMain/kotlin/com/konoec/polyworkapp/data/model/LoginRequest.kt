package com.konoec.polyworkapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val dni: String,
    val clave: String
)





