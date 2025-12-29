package com.konoec.polyworkapp.domain.usecase

import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.model.Shift
import com.konoec.polyworkapp.domain.repository.HomeRepository

class GetActiveShiftUseCase(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<Shift?> {
        return homeRepository.getActiveShift(forceRefresh)
    }
}

