package com.konoec.polyworkapp.data.repository

import com.konoec.polyworkapp.data.local.AuthLocalDataSource
import com.konoec.polyworkapp.data.local.HomeLocalDataSource
import com.konoec.polyworkapp.data.remote.HomeRemoteDataSource
import com.konoec.polyworkapp.data.util.safeApiCall
import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.model.Shift
import com.konoec.polyworkapp.domain.model.Stats
import com.konoec.polyworkapp.domain.repository.HomeRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class HomeRepositoryImpl(
    private val remoteDataSource: HomeRemoteDataSource,
    private val authLocalDataSource: AuthLocalDataSource,
    private val homeLocalDataSource: HomeLocalDataSource
) : HomeRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getActiveShift(forceRefresh: Boolean): Result<Shift?> {
        // Intentar obtener del cache primero
        if (!forceRefresh) {
            val cachedShift = homeLocalDataSource.getCachedShift()
            if (cachedShift != null) {
                val shift = Shift(
                    id = cachedShift.id,
                    status = com.konoec.polyworkapp.domain.model.ShiftStatus.fromString(cachedShift.status),
                    scheduledStartTime = cachedShift.scheduledStartTime,
                    scheduledEndTime = cachedShift.scheduledEndTime,
                    nextShiftTime = cachedShift.nextShiftTime
                )
                return Result.Success(shift)
            }
        }

        // Si no hay cache o se fuerza refresh, obtener del servidor
        val token = authLocalDataSource.getToken().firstOrNull()
            ?: return Result.Error("No token found")

        return when (val result = safeApiCall { remoteDataSource.getActiveShift(token) }) {
            is Result.Success -> {
                val response = result.data

                if (response.header.code == 200) {
                    val shiftData = response.body.shift

                    if (shiftData != null) {
                        // Guardar en cache
                        homeLocalDataSource.saveShiftCache(
                            id = shiftData.id,
                            status = shiftData.status,
                            scheduledStartTime = shiftData.scheduledStartTime,
                            scheduledEndTime = shiftData.scheduledEndTime,
                            nextShiftTime = shiftData.nextShiftTime
                        )

                        val shift = Shift(
                            id = shiftData.id,
                            status = com.konoec.polyworkapp.domain.model.ShiftStatus.fromString(shiftData.status),
                            scheduledStartTime = shiftData.scheduledStartTime,
                            scheduledEndTime = shiftData.scheduledEndTime,
                            nextShiftTime = shiftData.nextShiftTime
                        )
                        Result.Success(shift)
                    } else {
                        Result.Success(null)
                    }
                } else {
                    Result.Error(response.header.message)
                }
            }
            is Result.Error -> result
        }
    }

    override suspend fun getStats(forceRefresh: Boolean): Result<Stats?> {
        // Intentar obtener del cache primero
        if (!forceRefresh) {
            val cachedStatsJson = homeLocalDataSource.getCachedStats()
            if (cachedStatsJson != null) {
                try {
                    val stats = json.decodeFromString<Stats>(cachedStatsJson)
                    return Result.Success(stats)
                } catch (e: Exception) {
                    // Si falla el parseo, continuar con la petición al servidor
                }
            }
        }

        // Si no hay cache o se fuerza refresh, obtener del servidor
        val token = authLocalDataSource.getToken().firstOrNull()
            ?: return Result.Error("No token found")

        return when (val result = safeApiCall { remoteDataSource.getStats(token) }) {
            is Result.Success -> {
                val response = result.data
                if (response.header.code == 200) {
                    val stats = Stats(
                        diasLaborados = response.body.diasLaborados,
                        puntualidad = response.body.puntualidad
                    )

                    // Guardar en cache
                    try {
                        val statsJson = json.encodeToString(stats)
                        homeLocalDataSource.saveStatsCache(statsJson)
                    } catch (e: Exception) {
                        // Ignorar error de serialización
                    }

                    Result.Success(stats)
                } else {
                    Result.Error(response.header.message)
                }
            }
            is Result.Error -> result
        }
    }
}

