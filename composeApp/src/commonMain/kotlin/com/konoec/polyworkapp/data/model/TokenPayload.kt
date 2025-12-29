package com.konoec.polyworkapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TokenPayload(
    val sub: String,
    val dni: String,
    val name: String,
    val iat: Long,
    val exp: Long
)
