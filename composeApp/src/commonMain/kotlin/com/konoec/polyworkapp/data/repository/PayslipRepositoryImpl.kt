package com.konoec.polyworkapp.data.repository

import com.konoec.polyworkapp.data.local.AuthLocalDataSource
import com.konoec.polyworkapp.data.model.ValidatePayslipRequest
import com.konoec.polyworkapp.data.remote.PayslipRemoteDataSource
import com.konoec.polyworkapp.data.util.safeApiCall
import com.konoec.polyworkapp.domain.model.Payslip
import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.repository.PayslipRepository
import com.konoec.polyworkapp.domain.repository.ValidatePayslipResult
import kotlinx.coroutines.flow.firstOrNull

class PayslipRepositoryImpl(
    private val remoteDataSource: PayslipRemoteDataSource,
    private val localDataSource: AuthLocalDataSource
) : PayslipRepository {

    override suspend fun getPayslips(): Result<List<Payslip>> {
        val token = localDataSource.getToken().firstOrNull()
            ?: return Result.Error("No token found")

        return when (val result = safeApiCall { remoteDataSource.getPayslips(token) }) {
            is Result.Success -> {
                val response = result.data
                if (response.header.code == 200 && response.body != null) {
                    val payslips = response.body.boletas.map { dto ->
                        Payslip(
                            id = dto.id,
                            numero = dto.numero,
                            periodo = dto.periodo,
                            mes = dto.mes,
                            periodoLabel = dto.periodoLabel,
                            trabajador = dto.trabajador,
                            dni = dto.dni,
                            fechaGeneracion = dto.fechaGeneracion,
                            validado = dto.validado == "S",
                            fechaValidacion = dto.fechaValidacion
                        )
                    }
                    Result.Success(payslips)
                } else {
                    Result.Error(response.header.message)
                }
            }
            is Result.Error -> result
        }
    }

    override suspend fun downloadPayslipPdf(boletaId: Int): Result<ByteArray> {
        val token = localDataSource.getToken().firstOrNull()
            ?: return Result.Error("No token found")

        return safeApiCall { remoteDataSource.downloadPayslipPdf(token, boletaId) }
    }

    override suspend fun validatePayslip(
        boletaId: Int,
        deviceId: String?,
        latitud: String?,
        longitud: String?
    ): Result<ValidatePayslipResult> {
        val token = localDataSource.getToken().firstOrNull()
            ?: return Result.Error("No token found")

        val request = ValidatePayslipRequest(
            boletaId = boletaId,
            deviceId = deviceId,
            latitud = latitud,
            longitud = longitud
        )

        return when (val result = safeApiCall { remoteDataSource.validatePayslip(token, request) }) {
            is Result.Success -> {
                val response = result.data
                if (response.header.code == 200 && response.body != null) {
                    Result.Success(
                        ValidatePayslipResult(
                            success = response.body.success,
                            message = response.body.message,
                            numero = response.body.numero,
                            fechaValidacion = response.body.fechaValidacion
                        )
                    )
                } else {
                    Result.Error(response.header.message)
                }
            }
            is Result.Error -> result
        }
    }
}
