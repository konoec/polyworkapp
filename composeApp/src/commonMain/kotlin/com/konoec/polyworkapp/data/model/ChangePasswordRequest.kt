package com.konoec.polyworkapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class ChangePasswordResponse(
    val body: ChangePasswordBody,
    val header: ResponseHeader
)

@Serializable
data class ChangePasswordBody(
    val success: Boolean,
    val message: String
)
