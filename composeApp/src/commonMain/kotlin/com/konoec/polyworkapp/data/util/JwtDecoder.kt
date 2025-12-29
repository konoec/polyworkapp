package com.konoec.polyworkapp.data.util

import com.konoec.polyworkapp.data.model.TokenPayload
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object JwtDecoder {

    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalEncodingApi::class)
    fun decodePayload(token: String): TokenPayload? {
        return try {

            val parts = token.split(".")

            if (parts.size != 3) {
                return null
            }

            val payload = parts[1]
            val decodedBytes = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).decode(payload)
            val jsonString = decodedBytes.decodeToString()

            val result = json.decodeFromString<TokenPayload>(jsonString)
            result
        } catch (e: Exception) {
            println("ERROR decoding JWT: ${e.message}")
            println("Stack trace: ${e.stackTraceToString()}")
            null
        }
    }
}

