package com.konoec.polyworkapp.domain.usecase

import com.konoec.polyworkapp.domain.repository.AuthRepository

class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}

