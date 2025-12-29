package com.konoec.polyworkapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ShiftResponse(
    val body: ShiftBody,
    val header: ResponseHeader
)

@Serializable
data class ShiftBody(
    val shift: ShiftData?
)

@Serializable
data class ShiftData(
    val id: String,
    val status: String,                        // "ACTIVE" o "COMPLETED"
    val scheduledStartTime: String,            // "2025-12-27T08:00" o "08:00"
    val scheduledEndTime: String,              // "2025-12-27T19:00" o "19:00"
    val nextShiftTime: String? = null          // "2025-12-29T08:00" (solo si completed)
)
