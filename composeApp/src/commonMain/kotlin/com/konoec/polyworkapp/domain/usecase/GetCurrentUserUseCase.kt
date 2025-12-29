package com.konoec.polyworkapp.domain.usecase

import com.konoec.polyworkapp.domain.model.User
import com.konoec.polyworkapp.domain.repository.AuthRepository

class GetCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}

