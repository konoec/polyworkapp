package com.konoec.polyworkapp.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.konoec.polyworkapp.di.AppModule
import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class LoginViewModel(
    private val loginUseCase: LoginUseCase = AppModule.loginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    init {
        // Carrusel automático que alterna entre mensajes cada 5 segundos
        viewModelScope.launch {
            while (true) {
                delay(5_000L) // 5 segundos
                _state.update {
                    it.copy(carouselIndex = if (it.carouselIndex == 0) 1 else 0)
                }
            }
        }
    }

    fun onDniChange(value: String) {
        // DNI solo números, máximo 8 dígitos
        val filtered = value.filter { it.isDigit() }.take(8)
        _state.update { it.copy(dni = filtered, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, errorMessage = null) }
    }

    fun login(onSuccess: () -> Unit) {
        val current = _state.value
        if (current.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = loginUseCase(current.dni, current.password)) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
}
