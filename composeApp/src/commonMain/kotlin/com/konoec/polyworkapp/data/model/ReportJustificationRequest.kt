package com.konoec.polyworkapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReportJustificationRequest(
    val attendanceId: String,
    val justification: String,
    val evidence: String? // Base64 o URL del archivo adjunto
)

@Serializable
data class ReportJustificationResponse(
    val body: ReportJustificationBody,
    val header: ResponseHeader
)

@Serializable
data class ReportJustificationBody(
    val success: Boolean,
    val message: String
)
