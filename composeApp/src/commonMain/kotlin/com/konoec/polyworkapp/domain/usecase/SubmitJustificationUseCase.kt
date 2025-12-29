package com.konoec.polyworkapp.domain.usecase

import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.repository.AttendanceRepository

class SubmitJustificationUseCase(
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(
        attendanceId: String,
        justification: String,
        evidence: String?
    ): Result<String> {
        return attendanceRepository.submitJustification(attendanceId, justification, evidence)
    }
}

