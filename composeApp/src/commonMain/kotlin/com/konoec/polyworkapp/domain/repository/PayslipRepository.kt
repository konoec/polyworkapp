package com.konoec.polyworkapp.domain.repository

import com.konoec.polyworkapp.domain.model.Payslip
import com.konoec.polyworkapp.domain.model.Result

data class ValidatePayslipResult(
    val success: Boolean,
    val message: String,
    val numero: String,
    val fechaValidacion: String
)

interface PayslipRepository {
    suspend fun getPayslips(): Result<List<Payslip>>
    suspend fun downloadPayslipPdf(boletaId: Int): Result<ByteArray>
    suspend fun validatePayslip(
        boletaId: Int,
        deviceId: String?,
        latitud: String?,
        longitud: String?
    ): Result<ValidatePayslipResult>
}
