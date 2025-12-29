package com.konoec.polyworkapp.presentation.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentsViewModel : ViewModel() {
    private val _state = MutableStateFlow(PaymentsState())
    val state = _state.asStateFlow()

    init {
        loadPayments()
    }

    private fun loadPayments() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(2500)

            val mockData = listOf(
                PaymentItem("Septiembre 2023", "S/. 1,800.00", "Pagado"),
                PaymentItem("Agosto 2023", "S/. 1,800.00", "Pagado"),
                PaymentItem("Julio 2023", "S/. 2,000.00", "Pagado (Incluye Gratificación)")
            )
            _state.update { it.copy(isLoading = false, payments = mockData) }
        }
    }
}