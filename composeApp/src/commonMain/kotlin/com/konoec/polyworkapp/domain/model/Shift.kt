package com.konoec.polyworkapp.domain.model

data class Shift(
    val id: String,
    val status: ShiftStatus,
    val scheduledStartTime: String,  // "2025-12-27T08:00" o "08:00"
    val scheduledEndTime: String,    // "2025-12-27T19:00" o "19:00"
    val nextShiftTime: String?       // "2025-12-29T08:00" (solo si está completed)
)

enum class ShiftStatus {
    ACTIVE,      // Está trabajando ahora
    COMPLETED;   // Ya terminó su turno

    companion object {
        fun fromString(value: String): ShiftStatus {
            return when (value.trim().uppercase()) {
                "ACTIVE" -> ACTIVE
                "COMPLETED" -> COMPLETED
                else -> COMPLETED
            }
        }
    }
}
