package com.konoec.polyworkapp.domain.usecase

import com.konoec.polyworkapp.domain.repository.AuthRepository
import com.konoec.polyworkapp.domain.model.User
import com.konoec.polyworkapp.domain.model.Result

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(dni: String, password: String): Result<User> {
        // Validaciones
        if (dni.isBlank()) {
            return Result.Error("El DNI es requerido")
        }

        if (password.isBlank()) {
            return Result.Error("La contraseña es requerida")
        }

        // Validar que el DNI tenga exactamente 8 dígitos
        if (dni.length != 8) {
            return Result.Error("El DNI debe tener exactamente 8 dígitos")
        }

        // Validar que todos los caracteres sean números
        if (!dni.all { it.isDigit() }) {
            return Result.Error("El DNI solo debe contener números")
        }

        return authRepository.login(dni, password)
    }
}


