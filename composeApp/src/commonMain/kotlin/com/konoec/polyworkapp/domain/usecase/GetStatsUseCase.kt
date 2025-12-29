package com.konoec.polyworkapp.domain.usecase

import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.model.Stats
import com.konoec.polyworkapp.domain.repository.HomeRepository

class GetStatsUseCase(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<Stats?> {
        return homeRepository.getStats(forceRefresh)
    }
}

