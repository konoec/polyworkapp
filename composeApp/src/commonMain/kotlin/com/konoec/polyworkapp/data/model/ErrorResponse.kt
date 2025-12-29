package com.konoec.polyworkapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val header: ResponseHeader
)

