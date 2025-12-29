package com.konoec.polyworkapp.domain.model

data class AttendanceRecord(
    val id: String,
    val date: String,
    val scheduledTime: String,
    val realIn: String?,
    val realOut: String?,
    val status: AttendanceStatus,
    val canReport: Boolean
)

enum class AttendanceStatus {
    ASISTENCIA,
    TARDANZA,
    FALTA,
    PROCESO;

    companion object {
        fun fromString(value: String): AttendanceStatus {
            return when (value.uppercase()) {
                "ASISTENCIA" -> ASISTENCIA
                "TARDANZA" -> TARDANZA
                "FALTA" -> FALTA
                "PROCESO" -> PROCESO
                else -> PROCESO
            }
        }
    }
}

data class Month(
    val id: Int,
    val name: String,
    val year: String
)

