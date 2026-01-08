package com.konoec.polyworkapp.di

import com.konoec.polyworkapp.data.local.AuthLocalDataSource
import com.konoec.polyworkapp.data.local.HomeLocalDataSource
import com.konoec.polyworkapp.data.local.createDataStorePreferences
import com.konoec.polyworkapp.data.remote.AttendanceRemoteDataSource
import com.konoec.polyworkapp.data.remote.AuthRemoteDataSource
import com.konoec.polyworkapp.data.remote.HomeRemoteDataSource
import com.konoec.polyworkapp.data.remote.ScheduleRemoteDataSource
import com.konoec.polyworkapp.data.repository.AttendanceRepositoryImpl
import com.konoec.polyworkapp.data.repository.AuthRepositoryImpl
import com.konoec.polyworkapp.data.repository.HomeRepositoryImpl
import com.konoec.polyworkapp.data.repository.ScheduleRepositoryImpl
import com.konoec.polyworkapp.domain.repository.AttendanceRepository
import com.konoec.polyworkapp.domain.repository.AuthRepository
import com.konoec.polyworkapp.domain.repository.HomeRepository
import com.konoec.polyworkapp.domain.repository.ScheduleRepository
import com.konoec.polyworkapp.domain.usecase.ChangePasswordUseCase
import com.konoec.polyworkapp.domain.usecase.GetActiveShiftUseCase
import com.konoec.polyworkapp.domain.usecase.GetAttendanceRecordsUseCase
import com.konoec.polyworkapp.domain.usecase.GetCurrentUserUseCase
import com.konoec.polyworkapp.domain.usecase.GetMotivosJustificacionUseCase
import com.konoec.polyworkapp.domain.usecase.GetScheduleUseCase
import com.konoec.polyworkapp.domain.usecase.GetStatsUseCase
import com.konoec.polyworkapp.domain.usecase.LoginUseCase
import com.konoec.polyworkapp.domain.usecase.LogoutUseCase
import com.konoec.polyworkapp.domain.usecase.SubmitJustificationUseCase

object AppModule {

    private val dataStore by lazy { createDataStorePreferences() }

    // Remote Data Sources
    private val authRemoteDataSource by lazy { AuthRemoteDataSource() }
    private val homeRemoteDataSource by lazy { HomeRemoteDataSource() }
    private val attendanceRemoteDataSource by lazy { AttendanceRemoteDataSource() }
    private val scheduleRemoteDataSource by lazy { ScheduleRemoteDataSource() }

    // Local Data Sources
    private val authLocalDataSource by lazy { AuthLocalDataSource(dataStore) }
    private val homeLocalDataSource by lazy { HomeLocalDataSource(dataStore) }

    // Repositories
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            remoteDataSource = authRemoteDataSource,
            localDataSource = authLocalDataSource,
            homeLocalDataSource = homeLocalDataSource
        )
    }

    val homeRepository: HomeRepository by lazy {
        HomeRepositoryImpl(
            remoteDataSource = homeRemoteDataSource,
            authLocalDataSource = authLocalDataSource,
            homeLocalDataSource = homeLocalDataSource
        )
    }

    val attendanceRepository: AttendanceRepository by lazy {
        AttendanceRepositoryImpl(
            remoteDataSource = attendanceRemoteDataSource,
            localDataSource = authLocalDataSource
        )
    }

    val scheduleRepository: ScheduleRepository by lazy {
        ScheduleRepositoryImpl(
            remoteDataSource = scheduleRemoteDataSource,
            localDataSource = authLocalDataSource
        )
    }

    // Use Cases
    val loginUseCase by lazy { LoginUseCase(authRepository) }
    val logoutUseCase by lazy { LogoutUseCase(authRepository) }
    val changePasswordUseCase by lazy { ChangePasswordUseCase(authRepository) }
    val getCurrentUserUseCase by lazy { GetCurrentUserUseCase(authRepository) }
    val getActiveShiftUseCase by lazy { GetActiveShiftUseCase(homeRepository) }
    val getStatsUseCase by lazy { GetStatsUseCase(homeRepository) }
    val getAttendanceRecordsUseCase by lazy { GetAttendanceRecordsUseCase(attendanceRepository) }
    val getMotivosJustificacionUseCase by lazy { GetMotivosJustificacionUseCase(attendanceRepository) }
    val submitJustificationUseCase by lazy { SubmitJustificationUseCase(attendanceRepository) }
    val getScheduleUseCase by lazy { GetScheduleUseCase(scheduleRepository) }
}

