package com.konoec.polyworkapp.domain.usecase

import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.repository.PayslipRepository

class DownloadPayslipUseCase(
    private val payslipRepository: PayslipRepository
) {
    suspend operator fun invoke(boletaId: Int): Result<ByteArray> {
        return payslipRepository.downloadPayslipPdf(boletaId)
    }
}
