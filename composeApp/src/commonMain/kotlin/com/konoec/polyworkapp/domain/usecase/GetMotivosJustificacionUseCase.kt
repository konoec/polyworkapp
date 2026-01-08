package com.konoec.polyworkapp.domain.usecase

import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.repository.AttendanceRepository
import com.konoec.polyworkapp.domain.repository.MotivoJustificacion

class GetMotivosJustificacionUseCase(
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(): Result<List<MotivoJustificacion>> {
        return attendanceRepository.getMotivosJustificacion()
    }
}

