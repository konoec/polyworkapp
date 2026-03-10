package com.konoec.polyworkapp.data.remote

import com.konoec.polyworkapp.data.model.PayslipListResponse
import com.konoec.polyworkapp.data.model.ValidatePayslipRequest
import com.konoec.polyworkapp.data.model.ValidatePayslipResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpHeaders

class PayslipRemoteDataSource(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val client = HttpClientFactory.create()

    suspend fun getPayslips(token: String): PayslipListResponse {
        return client.get("$baseUrl/payslips") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun downloadPayslipPdf(token: String, boletaId: Int): ByteArray {
        val response = client.get("$baseUrl/payslips/download?boletaId=$boletaId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        return response.readRawBytes()
    }

    suspend fun validatePayslip(token: String, request: ValidatePayslipRequest): ValidatePayslipResponse {
        return client.post("$baseUrl/payslips/validate") {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(request)
        }.body()
    }
}
