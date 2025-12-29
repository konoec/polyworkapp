package com.konoec.polyworkapp.domain.model

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Exception? = null, val isAuthError: Boolean = false) : Result<Nothing>()
}

