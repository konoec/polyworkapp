package com.konoec.polyworkapp.data.util

import com.konoec.polyworkapp.data.model.ErrorResponse
import com.konoec.polyworkapp.domain.model.Result
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json

suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): Result<T> {
    return try {
        val response = apiCall()
        Result.Success(response)
    } catch (e: ClientRequestException) {
        val statusCode = e.response.status
        val isAuthError = statusCode == HttpStatusCode.Unauthorized

        try {
            val errorBody = e.response.bodyAsText()
            val json = Json { ignoreUnknownKeys = true }
            val errorResponse = json.decodeFromString<ErrorResponse>(errorBody)

            val message = when {
                isAuthError -> "Tu sesión ha expirado. Por favor, inicia sesión nuevamente."
                else -> errorResponse.header.message
            }

            Result.Error(message = message, isAuthError = isAuthError)
        } catch (parseError: Exception) {
            val message = when {
                isAuthError -> "Tu sesión ha expirado. Por favor, inicia sesión nuevamente."
                else -> "Error al procesar la respuesta del servidor"
            }
            Result.Error(message = message, isAuthError = isAuthError)
        }
    } catch (e: Exception) {
        val message = when {
            e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                "No se pudo conectar al servidor. Verifica tu conexión."
            e.message?.contains("timeout", ignoreCase = true) == true ->
                "Tiempo de espera agotado. Intenta nuevamente."
            e.message?.contains("Connection refused", ignoreCase = true) == true ->
                "El servidor no está disponible. Contacta a soporte."
            e.message?.contains("No address associated", ignoreCase = true) == true ->
                "No se pudo resolver la dirección del servidor."
            else ->
                "Error de conexión. Verifica tu red e intenta nuevamente."
        }
        Result.Error(message = message, exception = e, isAuthError = false)
    }
}


