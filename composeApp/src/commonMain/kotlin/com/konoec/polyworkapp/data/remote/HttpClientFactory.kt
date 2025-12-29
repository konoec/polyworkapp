package com.konoec.polyworkapp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(token: String? = null): HttpClient {
        return HttpClient {
            // JSON serialization
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                    encodeDefaults = true
                    coerceInputValues = true
                    explicitNulls = false
                })
            }

            // Logging
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.ALL
            }

            // Timeouts
            install(HttpTimeout) {
                requestTimeoutMillis = ApiConfig.READ_TIMEOUT
                connectTimeoutMillis = ApiConfig.CONNECT_TIMEOUT
                socketTimeoutMillis = ApiConfig.READ_TIMEOUT
            }

            // Default headers
            defaultRequest {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                token?.let {
                    header(HttpHeaders.Authorization, "Bearer $it")
                }
            }
        }
    }
}
