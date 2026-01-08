package com.konoec.polyworkapp.domain.repository

import com.konoec.polyworkapp.domain.model.AttendanceRecord
import com.konoec.polyworkapp.domain.model.Month
import com.konoec.polyworkapp.domain.model.Result

interface AttendanceRepository {
    suspend fun getAttendanceRecords(monthId: Int, year: Int): Result<Pair<List<AttendanceRecord>, List<Month>>>
    suspend fun getMotivosJustificacion(): Result<List<MotivoJustificacion>>
    suspend fun submitJustification(
        attendanceId: String,
        description: String,
        deviceId: String?,
        imageBytes: ByteArray?,
        fileName: String?,
        motivoId: Int?
    ): Result<String>
}

data class MotivoJustificacion(
    val id: Int,
    val descripcion: String
)
