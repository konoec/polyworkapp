package com.konoec.polyworkapp.data.remote

import com.konoec.polyworkapp.data.model.ChangePasswordRequest
import com.konoec.polyworkapp.data.model.ChangePasswordResponse
import com.konoec.polyworkapp.data.model.LoginRequest
import com.konoec.polyworkapp.data.model.LoginResponse
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class AuthRemoteDataSource(
    private val baseUrl: String = ApiConfig.BASE_URL
) {

    private val client = HttpClientFactory.create()

    suspend fun login(dni: String, password: String): LoginResponse {
        return client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(dni = dni, clave = password))
        }.body()
    }

    suspend fun changePassword(token: String, currentPassword: String, newPassword: String): ChangePasswordResponse {
        return client.post("$baseUrl/auth/change-password") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(ChangePasswordRequest(currentPassword = currentPassword, newPassword = newPassword))
        }.body()
    }

    fun close() {
        client.close()
    }
}

