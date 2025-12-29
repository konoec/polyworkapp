package com.konoec.polyworkapp.data.remote

import com.konoec.polyworkapp.data.model.AttendanceResponse
import com.konoec.polyworkapp.data.model.ReportJustificationRequest
import com.konoec.polyworkapp.data.model.ReportJustificationResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class AttendanceRemoteDataSource(
    private val baseUrl: String = ApiConfig.BASE_URL
) {

    private val client = HttpClientFactory.create()

    suspend fun getAttendanceRecords(token: String, monthId: Int): AttendanceResponse {
        return client.get("$baseUrl/attendance?monthId=$monthId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun submitJustification(
        token: String,
        request: ReportJustificationRequest
    ): ReportJustificationResponse {
        return client.post("$baseUrl/attendance/report") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    fun close() {
        client.close()
    }
}

