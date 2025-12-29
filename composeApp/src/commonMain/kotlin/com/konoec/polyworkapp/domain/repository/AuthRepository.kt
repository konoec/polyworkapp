package com.konoec.polyworkapp.domain.repository

import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(dni: String, password: String): Result<User>
    suspend fun logout()
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<String>
    fun getToken(): Flow<String?>
    suspend fun isLoggedIn(): Boolean
    suspend fun getCurrentUser(): User?
}

