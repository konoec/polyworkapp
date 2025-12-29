package com.konoec.polyworkapp.presentation.home

import com.konoec.polyworkapp.domain.model.Shift
import com.konoec.polyworkapp.domain.model.Stats
import com.konoec.polyworkapp.domain.model.User

data class HomeState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val userName: String = "",
    val activeShift: Shift? = null,
    val stats: Stats? = null,
    val error: String? = null,
    val sessionExpired: Boolean = false
)

