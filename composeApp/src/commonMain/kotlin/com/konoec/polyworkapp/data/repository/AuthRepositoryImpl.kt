package com.konoec.polyworkapp.data.repository

import com.konoec.polyworkapp.data.local.AuthLocalDataSource
import com.konoec.polyworkapp.data.local.HomeLocalDataSource
import com.konoec.polyworkapp.data.remote.AuthRemoteDataSource
import com.konoec.polyworkapp.data.util.JwtDecoder
import com.konoec.polyworkapp.data.util.safeApiCall
import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.model.User
import com.konoec.polyworkapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    private val localDataSource: AuthLocalDataSource,
    private val homeLocalDataSource: HomeLocalDataSource
) : AuthRepository {

    override suspend fun login(dni: String, password: String): Result<User> {
        // Limpiar cache del usuario anterior antes de hacer login
        homeLocalDataSource.clearAllHomeCache()

        return when (val result = safeApiCall { remoteDataSource.login(dni, password) }) {
            is Result.Success -> {
                val response = result.data

                // Verificar el código de la respuesta
                if (response.header.code == 200) {
                    val token = response.body.token
                    val payload = JwtDecoder.decodePayload(token)

                    if (payload != null) {
                        // Guardar datos localmente
                        localDataSource.saveAuthData(
                            token = token,
                            userId = payload.sub,
                            dni = payload.dni,
                            name = payload.name
                        )

                        val user = User(
                            id = payload.sub,
                            dni = payload.dni,
                            name = payload.name,
                            token = token
                        )
                        Result.Success(user)
                    } else {
                        Result.Error("Error al procesar la respuesta del servidor")
                    }
                } else {
                    // Manejar errores que vienen en el response normal (código != 200)
                    Result.Error(response.header.message)
                }
            }
            is Result.Error -> result
        }
    }

    override suspend fun logout() {
        // Limpiar datos de autenticación
        localDataSource.clearAuthData()
        // Limpiar cache de Home para evitar mostrar datos del usuario anterior
        homeLocalDataSource.clearAllHomeCache()
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<String> {
        val token = localDataSource.getToken().firstOrNull()
            ?: return Result.Error("No token found")

        return when (val result = safeApiCall {
            remoteDataSource.changePassword(token, currentPassword, newPassword)
        }) {
            is Result.Success -> {
                val response = result.data
                if (response.header.code == 200 && response.body.success) {
                    Result.Success(response.body.message)
                } else {
                    Result.Error(response.body.message)
                }
            }
            is Result.Error -> result
        }
    }

    override fun getToken(): Flow<String?> {
        return localDataSource.getToken()
    }

    override suspend fun isLoggedIn(): Boolean {
        return localDataSource.getToken().firstOrNull() != null
    }

    override suspend fun getCurrentUser(): User? {
        val token = localDataSource.getToken().firstOrNull() ?: return null
        val userId = localDataSource.getUserId().firstOrNull() ?: return null
        val dni = localDataSource.getUserDni().firstOrNull() ?: return null
        val name = localDataSource.getUserName().firstOrNull() ?: return null

        return User(
            id = userId,
            dni = dni,
            name = name,
            token = token
        )
    }
}
