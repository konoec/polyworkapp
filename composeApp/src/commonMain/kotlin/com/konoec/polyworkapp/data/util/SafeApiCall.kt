package com.konoec.polyworkapp.data.util

import com.konoec.polyworkapp.data.model.ErrorResponse
import com.konoec.polyworkapp.domain.model.Result
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
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
        // Errores 4xx (400, 401, 403, 404, etc.)
        handleHttpException(e.response.status, e.response.bodyAsText())
    } catch (e: ServerResponseException) {
        // Errores 5xx (500, 502, 503, etc.)
        handleHttpException(e.response.status, e.response.bodyAsText())
    } catch (e: Exception) {
        // Errores de red (sin conexión, timeout, etc.)
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

private fun handleHttpException(statusCode: HttpStatusCode, errorBody: String): Result.Error {
    val isAuthError = statusCode == HttpStatusCode.Unauthorized

    return try {
        val json = Json { ignoreUnknownKeys = true }
        val errorResponse = json.decodeFromString<ErrorResponse>(errorBody)

        // Usar siempre el mensaje que viene del servidor
        Result.Error(message = errorResponse.header.message, isAuthError = isAuthError)
    } catch (parseError: Exception) {
        // Si no se puede parsear la respuesta del servidor
        val message = when {
            isAuthError -> "Tu sesión ha expirado. Por favor, inicia sesión nuevamente."
            statusCode.value in 500..599 -> "Error del servidor. Intenta nuevamente más tarde."
            else -> "Error al procesar la respuesta del servidor (${statusCode.value})"
        }
        Result.Error(message = message, isAuthError = isAuthError)
    }
}
