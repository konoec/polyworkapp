package com.konoec.polyworkapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val body: LoginBody,
    val header: ResponseHeader
)

@Serializable
data class LoginBody(
    val token: String
)

@Serializable
data class ResponseHeader(
    val code: Int,
    val message: String
)

