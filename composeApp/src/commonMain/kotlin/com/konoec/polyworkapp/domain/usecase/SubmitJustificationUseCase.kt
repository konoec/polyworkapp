package com.konoec.polyworkapp.domain.usecase

import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.repository.AttendanceRepository

class SubmitJustificationUseCase(
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(
        attendanceId: String,
        description: String,
        deviceId: String?,
        imageBytes: ByteArray?,
        fileName: String?,
        motivoId: Int?
    ): Result<String> {
        return attendanceRepository.submitJustification(attendanceId, description, deviceId, imageBytes, fileName, motivoId)
    }
}

