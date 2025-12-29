package com.konoec.polyworkapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleResponse(
    val body: ScheduleBody,
    val header: ResponseHeader
)

@Serializable
data class ScheduleBody(
    val shifts: List<ScheduleShift>
)

@Serializable
data class ScheduleShift(
    val day: String,            // "Lunes", "Martes", etc.
    val date: String,           // "22/12/2025"
    val time: String,           // "08:00 - 17:30"
    val shiftType: String,      // "Turno Mañana", "Turno Noche", "Descanso"
    val confirmed: Boolean      // true/false
)
