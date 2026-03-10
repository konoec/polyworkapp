package com.konoec.polyworkapp.presentation.login

data class LoginState(
    val dni: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val carouselIndex: Int = 0, // 0 = contacto, 1 = recordatorio contraseña
)
