package com.konoec.polyworkapp.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.konoec.polyworkapp.di.AppModule
import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.usecase.GetScheduleUseCase
import com.konoec.polyworkapp.presentation.util.calculateShiftDuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val getScheduleUseCase: GetScheduleUseCase = AppModule.getScheduleUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ScheduleState())
    val state = _state.asStateFlow()

    init {
        loadSchedule()
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            when (val result = getScheduleUseCase()) {
                is Result.Success -> {
                    val shifts = result.data.map { shift ->
                        ScheduleItem(
                            day = shift.day,
                            date = shift.date,
                            time = shift.time,
                            shiftType = shift.shiftType,
                            duration = calculateShiftDuration(shift.time), // Siempre calcular
                            isConfirmed = shift.confirmed
                        )
                    }
                    _state.update { it.copy(isLoading = false, shifts = shifts) }
                }
                is Result.Error -> {
                    if (result.isAuthError) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                sessionExpired = true,
                                shifts = emptyList()
                            )
                        }
                    } else {
                        _state.update { it.copy(isLoading = false, shifts = emptyList()) }
                    }
                }
            }
        }
    }

    fun refreshSchedule() {
        loadSchedule()
    }

    fun clearState() {
        _state.value = ScheduleState()
    }

    /**
     * Recarga los horarios si es necesario.
     * Se debe llamar cuando se vuelve a la pantalla después de un logout/login.
     */
    fun reloadIfNeeded() {
        if (_state.value.shifts.isEmpty() && !_state.value.isLoading) {
            loadSchedule()
        }
    }
}


