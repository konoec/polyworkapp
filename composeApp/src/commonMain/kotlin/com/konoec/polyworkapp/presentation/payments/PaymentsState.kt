package com.konoec.polyworkapp.presentation.payments

import com.konoec.polyworkapp.domain.model.Payslip

data class PaymentsState(
    val isLoading: Boolean = false,
    val payslips: List<Payslip> = emptyList(),
    val selectedYear: Int = 0,
    val availableYears: List<Int> = emptyList(),
    val errorMessage: String? = null,
    val sessionExpired: Boolean = false,

    // Descarga
    val isDownloading: Boolean = false,
    val downloadedPayslipIds: Set<Int> = emptySet(),

    // Overlay de boleta seleccionada
    val selectedPayslip: Payslip? = null,
    val isValidating: Boolean = false,
    val confirmChecked: Boolean = false,
    val validateMessage: String? = null,
    val validateSuccess: Boolean? = null
) {
    val filteredPayslips: List<Payslip>
        get() = if (selectedYear == 0) payslips
        else payslips.filter { it.periodo == selectedYear }
}