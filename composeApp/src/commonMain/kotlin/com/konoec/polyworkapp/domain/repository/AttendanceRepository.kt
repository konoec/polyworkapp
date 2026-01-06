package com.konoec.polyworkapp.domain.repository

import com.konoec.polyworkapp.domain.model.AttendanceRecord
import com.konoec.polyworkapp.domain.model.Month
import com.konoec.polyworkapp.domain.model.Result

interface AttendanceRepository {
    suspend fun getAttendanceRecords(monthId: Int, year: Int): Result<Pair<List<AttendanceRecord>, List<Month>>>
    suspend fun submitJustification(
        attendanceId: String,
        description: String,
        deviceId: String?,
        imageBytes: ByteArray?
    ): Result<String>
}

