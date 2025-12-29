package com.konoec.polyworkapp.presentation.login

data class LoginState(
    val dni: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
