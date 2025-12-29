package com.konoec.polyworkapp.presentation.payments

data class PaymentsState(
    val isLoading: Boolean = false,
    val payments: List<PaymentItem> = emptyList()
)

data class PaymentItem(val month: String, val amount: String, val status: String)