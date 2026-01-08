package com.konoec.polyworkapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReportJustificationRequest(
    val attendanceId: String,
    val description: String,
    val deviceId: String? = null,
    val evidenceUrl: String? = null,
    val motivoId: Int? = null
)

@Serializable
data class ReportJustificationResponse(
    val body: ReportJustificationBody,
    val header: ResponseHeader
)

@Serializable
data class ReportJustificationBody(
    val success: Boolean,
    val message: String,
    val reportId: String?
)
