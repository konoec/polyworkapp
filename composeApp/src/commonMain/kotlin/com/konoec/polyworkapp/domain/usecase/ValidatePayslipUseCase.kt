package com.konoec.polyworkapp.domain.usecase

import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.repository.PayslipRepository
import com.konoec.polyworkapp.domain.repository.ValidatePayslipResult

class ValidatePayslipUseCase(
    private val payslipRepository: PayslipRepository
) {
    suspend operator fun invoke(
        boletaId: Int,
        deviceId: String?,
        latitud: String?,
        longitud: String?
    ): Result<ValidatePayslipResult> {
        return payslipRepository.validatePayslip(boletaId, deviceId, latitud, longitud)
    }
}
