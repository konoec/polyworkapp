package com.konoec.polyworkapp.data.remote

import com.konoec.polyworkapp.data.model.ScheduleResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

class ScheduleRemoteDataSource(
    private val baseUrl: String = ApiConfig.BASE_URL
) {

    private val client = HttpClientFactory.create()

    suspend fun getSchedule(token: String): ScheduleResponse {
        return client.get("$baseUrl/schedule") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    fun close() {
        client.close()
    }
}
