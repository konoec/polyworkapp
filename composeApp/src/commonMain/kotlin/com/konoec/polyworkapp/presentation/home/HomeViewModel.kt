package com.konoec.polyworkapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.konoec.polyworkapp.di.AppModule
import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.usecase.ChangePasswordUseCase
import com.konoec.polyworkapp.domain.usecase.GetActiveShiftUseCase
import com.konoec.polyworkapp.domain.usecase.GetCurrentUserUseCase
import com.konoec.polyworkapp.domain.usecase.GetStatsUseCase
import com.konoec.polyworkapp.domain.usecase.LogoutUseCase
import com.konoec.polyworkapp.presentation.ViewModelRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase = AppModule.getCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase = AppModule.logoutUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase = AppModule.changePasswordUseCase,
    private val getActiveShiftUseCase: GetActiveShiftUseCase = AppModule.getActiveShiftUseCase,
    private val getStatsUseCase: GetStatsUseCase = AppModule.getStatsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadAllData()
    }

    private fun loadAllData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val user = getCurrentUserUseCase()
            if (user == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error al cargar datos del usuario"
                )
                return@launch
            }

            _state.value = _state.value.copy(
                user = user,
                userName = user.name
            )

            when (val shiftResult = getActiveShiftUseCase(forceRefresh = true)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(activeShift = shiftResult.data)
                }
                is Result.Error -> {
                    if (shiftResult.isAuthError) {
                        handleSessionExpired()
                        return@launch
                    }
                }
            }

            when (val statsResult = getStatsUseCase(forceRefresh = true)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        stats = statsResult.data,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    if (statsResult.isAuthError) {
                        handleSessionExpired()
                        return@launch
                    }
                    _state.value = _state.value.copy(
                        stats = null,
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun handleSessionExpired() {
        logoutUseCase()
        _state.value = _state.value.copy(
            isLoading = false,
            sessionExpired = true,
            error = "Tu sesión ha expirado. Por favor, inicia sesión nuevamente."
        )
    }

    fun refreshData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            // Recargar datos del usuario
            val user = getCurrentUserUseCase()
            if (user == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error al cargar datos del usuario"
                )
                return@launch
            }

            _state.value = _state.value.copy(
                user = user,
                userName = user.name
            )

            when (val shiftResult = getActiveShiftUseCase(forceRefresh = true)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(activeShift = shiftResult.data)
                }
                is Result.Error -> {
                    if (shiftResult.isAuthError) {
                        handleSessionExpired()
                        return@launch
                    }
                }
            }

            when (val statsResult = getStatsUseCase(forceRefresh = true)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        stats = statsResult.data,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    if (statsResult.isAuthError) {
                        handleSessionExpired()
                        return@launch
                    }
                    _state.value = _state.value.copy(isLoading = false)
                }
            }
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = changePasswordUseCase(currentPassword, newPassword)) {
                is Result.Success -> {
                    onSuccess(result.data)
                }
                is Result.Error -> {
                    if (result.isAuthError) {
                        handleSessionExpired()
                    } else {
                        onError(result.message)
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            // Limpiar todos los ViewModels
            ViewModelRegistry.clearAllViewModels()
            // Limpiar el estado del ViewModel actual
            _state.value = HomeState()
        }
    }
}

