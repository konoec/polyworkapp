package com.konoec.polyworkapp.data.repository

import com.konoec.polyworkapp.data.local.AuthLocalDataSource
import com.konoec.polyworkapp.data.remote.ScheduleRemoteDataSource
import com.konoec.polyworkapp.data.util.safeApiCall
import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.model.ScheduleShift
import com.konoec.polyworkapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.firstOrNull

class ScheduleRepositoryImpl(
    private val remoteDataSource: ScheduleRemoteDataSource,
    private val localDataSource: AuthLocalDataSource
) : ScheduleRepository {

    override suspend fun getSchedule(): Result<List<ScheduleShift>> {
        val token = localDataSource.getToken().firstOrNull()
            ?: return Result.Error("No token found")

        return when (val result = safeApiCall { remoteDataSource.getSchedule(token) }) {
            is Result.Success -> {
                val response = result.data
                if (response.header.code == 200) {
                    val shifts = response.body.shifts.map { shiftData ->
                        ScheduleShift(
                            day = shiftData.day,
                            date = shiftData.date,
                            time = shiftData.time,
                            shiftType = shiftData.shiftType,
                            confirmed = shiftData.confirmed
                        )
                    }
                    Result.Success(shifts)
                } else {
                    Result.Error(response.header.message)
                }
            }
            is Result.Error -> result
        }
    }
}
