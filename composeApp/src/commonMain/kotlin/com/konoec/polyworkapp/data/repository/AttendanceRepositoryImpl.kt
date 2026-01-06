package com.konoec.polyworkapp.data.repository

import com.konoec.polyworkapp.data.local.AuthLocalDataSource
import com.konoec.polyworkapp.data.model.ReportJustificationRequest
import com.konoec.polyworkapp.data.remote.AttendanceRemoteDataSource
import com.konoec.polyworkapp.data.util.safeApiCall
import com.konoec.polyworkapp.domain.model.AttendanceRecord
import com.konoec.polyworkapp.domain.model.AttendanceStatus
import com.konoec.polyworkapp.domain.model.Month
import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AttendanceRepositoryImpl(
    private val remoteDataSource: AttendanceRemoteDataSource,
    private val localDataSource: AuthLocalDataSource
) : AttendanceRepository {

    override suspend fun getAttendanceRecords(monthId: Int, year: Int): Result<Pair<List<AttendanceRecord>, List<Month>>> {
        val token = localDataSource.getToken().firstOrNull()
            ?: return Result.Error("No token found")

        return when (val result = safeApiCall { remoteDataSource.getAttendanceRecords(token, monthId, year) }) {
            is Result.Success -> {
                val response = result.data
                if (response.header.code == 200 && response.body != null) {
                    val records = response.body.records.map { recordData ->
                        AttendanceRecord(
                            id = recordData.id,
                            date = recordData.date,
                            scheduledTime = recordData.scheduledTime,
                            realIn = recordData.realIn,
                            realOut = recordData.realOut,
                            status = AttendanceStatus.fromString(recordData.status),
                            canReport = recordData.canReport
                        )
                    }

                    // Si el API no envía availableMonths, generar los meses del año actual
                    val months = response.body.availableMonths?.map { monthData ->
                        Month(
                            id = monthData.id,
                            name = monthData.name,
                            year = monthData.year
                        )
                    } ?: generateCurrentYearMonths()

                    Result.Success(Pair(records, months))
                } else {
                    Result.Error(response.header.message)
                }
            }
            is Result.Error -> result
        }
    }

    // Genera los 12 meses del año actual si el API no los envía
    private fun generateCurrentYearMonths(): List<Month> {
        val now = kotlin.time.Clock.System.now()
        val currentYear = now.toLocalDateTime(TimeZone.currentSystemDefault())
            .year
            .toString()

        return listOf(
            Month(1, "Enero", currentYear),
            Month(2, "Febrero", currentYear),
            Month(3, "Marzo", currentYear),
            Month(4, "Abril", currentYear),
            Month(5, "Mayo", currentYear),
            Month(6, "Junio", currentYear),
            Month(7, "Julio", currentYear),
            Month(8, "Agosto", currentYear),
            Month(9, "Septiembre", currentYear),
            Month(10, "Octubre", currentYear),
            Month(11, "Noviembre", currentYear),
            Month(12, "Diciembre", currentYear)
        )
    }

    override suspend fun submitJustification(
        attendanceId: String,
        description: String,
        deviceId: String?,
        imageBytes: ByteArray?
    ): Result<String> {
        val token = localDataSource.getToken().firstOrNull()
            ?: return Result.Error("No token found")

        val request = ReportJustificationRequest(
            attendanceId = attendanceId,
            description = description,
            deviceId = deviceId,
            evidenceUrl = null // Se usa null cuando se envía archivo binario
        )

        return when (val result = safeApiCall { remoteDataSource.submitJustification(token, request, imageBytes) }) {
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
}

