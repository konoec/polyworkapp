package com.konoec.polyworkapp.domain.usecase

import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.model.ScheduleShift
import com.konoec.polyworkapp.domain.repository.ScheduleRepository

class GetScheduleUseCase(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(): Result<List<ScheduleShift>> {
        return scheduleRepository.getSchedule()
    }
}

