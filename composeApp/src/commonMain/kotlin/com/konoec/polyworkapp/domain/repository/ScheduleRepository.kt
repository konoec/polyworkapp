package com.konoec.polyworkapp.domain.repository

import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.model.ScheduleShift

interface ScheduleRepository {
    suspend fun getSchedule(): Result<List<ScheduleShift>>
}

