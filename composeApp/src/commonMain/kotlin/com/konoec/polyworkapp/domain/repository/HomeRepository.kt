package com.konoec.polyworkapp.domain.repository

import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.model.Shift
import com.konoec.polyworkapp.domain.model.Stats

interface HomeRepository {
    suspend fun getActiveShift(forceRefresh: Boolean = false): Result<Shift?>
    suspend fun getStats(forceRefresh: Boolean = false): Result<Stats?>
}
