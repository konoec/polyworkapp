package com.konoec.polyworkapp.domain.usecase

import com.konoec.polyworkapp.domain.model.Payslip
import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.repository.PayslipRepository

class GetPayslipsUseCase(
    private val payslipRepository: PayslipRepository
) {
    suspend operator fun invoke(): Result<List<Payslip>> {
        return payslipRepository.getPayslips()
    }
}
