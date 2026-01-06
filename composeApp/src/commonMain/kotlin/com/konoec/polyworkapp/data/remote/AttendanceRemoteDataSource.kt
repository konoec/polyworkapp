package com.konoec.polyworkapp.data.remote

import com.konoec.polyworkapp.data.model.AttendanceResponse
import com.konoec.polyworkapp.data.model.ReportJustificationRequest
import com.konoec.polyworkapp.data.model.ReportJustificationResponse
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AttendanceRemoteDataSource(
    private val baseUrl: String = ApiConfig.BASE_URL
) {

    private val client = HttpClientFactory.create()

    suspend fun getAttendanceRecords(token: String, monthId: Int, year: Int): AttendanceResponse {
        return client.get("$baseUrl/attendance?monthId=$monthId&year=$year") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun submitJustification(
        token: String,
        request: ReportJustificationRequest,
        imageBytes: ByteArray?
    ): ReportJustificationResponse {
        return client.submitFormWithBinaryData(
            url = "$baseUrl/attendance/report",
            formData = formData {
                // 1. Parte DATA: JSON serializado a String
                append("data", Json.encodeToString(request), Headers.build {
                    append(HttpHeaders.ContentType, "application/json")
                })

                // 2. Parte FILE: Binario (Solo si existe)
                if (imageBytes != null) {
                    append("file", imageBytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"evidencia.jpg\"")
                    })
                }
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    fun close() {
        client.close()
    }
}

