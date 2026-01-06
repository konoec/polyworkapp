package com.konoec.polyworkapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceResponse(
    val body: AttendanceBody? = null,
    val header: ResponseHeader
)

@Serializable
data class AttendanceBody(
    val records: List<AttendanceRecord>,
    val availableMonths: List<MonthData>? = null // Opcional: el API puede no enviarlo
)

@Serializable
data class AttendanceRecord(
    val id: String,
    val date: String,
    val scheduledTime: String,
    val realIn: String?,
    val realOut: String?,
    val status: String, // "ASISTENCIA", "TARDANZA", "FALTA", "PROCESO"
    val canReport: Boolean
)

@Serializable
data class MonthData(
    val id: Int,
    val name: String,
    val year: String
)
