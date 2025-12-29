package com.konoec.polyworkapp.domain.usecase

import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.repository.AuthRepository

class ChangePasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(currentPassword: String, newPassword: String): Result<String> {
        // Validaciones
        if (currentPassword.isBlank()) {
            return Result.Error("La contraseña actual es requerida")
        }

        if (newPassword.isBlank()) {
            return Result.Error("La nueva contraseña es requerida")
        }

        if (newPassword.length < 6) {
            return Result.Error("La nueva contraseña debe tener al menos 6 caracteres")
        }

        if (currentPassword == newPassword) {
            return Result.Error("La nueva contraseña debe ser diferente a la actual")
        }

        return authRepository.changePassword(currentPassword, newPassword)
    }
}

