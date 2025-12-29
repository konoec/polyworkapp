package com.konoec.polyworkapp.data.remote

import com.konoec.polyworkapp.data.model.ShiftResponse
import com.konoec.polyworkapp.data.model.StatsResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

class HomeRemoteDataSource(
    private val baseUrl: String = ApiConfig.BASE_URL
) {

    private val client = HttpClientFactory.create()

    suspend fun getActiveShift(token: String): ShiftResponse {
        return client.get("$baseUrl/shifts/active") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun getStats(token: String): StatsResponse {
        return client.get("$baseUrl/stats") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    fun close() {
        client.close()
    }
}

