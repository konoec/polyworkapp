package com.konoec.polyworkapp.domain.usecase

import com.konoec.polyworkapp.domain.repository.AttendanceRepository
import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.model.Month
import com.konoec.polyworkapp.domain.model.AttendanceRecord

class GetAttendanceRecordsUseCase(
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(monthId: Int): Result<Pair<List<AttendanceRecord>, List<Month>>> {
        return attendanceRepository.getAttendanceRecords(monthId)
    }
}
