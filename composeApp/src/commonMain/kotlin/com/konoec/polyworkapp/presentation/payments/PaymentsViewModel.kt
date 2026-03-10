package com.konoec.polyworkapp.presentation.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.konoec.polyworkapp.di.AppModule
import com.konoec.polyworkapp.domain.model.Payslip
import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.usecase.DownloadPayslipUseCase
import com.konoec.polyworkapp.domain.usecase.GetPayslipsUseCase
import com.konoec.polyworkapp.domain.usecase.ValidatePayslipUseCase
import com.konoec.polyworkapp.platform.DeviceInfo
import com.konoec.polyworkapp.platform.PdfHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class PaymentsViewModel(
    private val getPayslipsUseCase: GetPayslipsUseCase = AppModule.getPayslipsUseCase,
    private val downloadPayslipUseCase: DownloadPayslipUseCase = AppModule.downloadPayslipUseCase,
    private val validatePayslipUseCase: ValidatePayslipUseCase = AppModule.validatePayslipUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentsState())
    val state = _state.asStateFlow()

    init {
        val currentYear = getCurrentYear()
        val years = (2020..currentYear).toList().reversed()
        _state.update { it.copy(selectedYear = currentYear, availableYears = years) }
        loadPayslips()
    }

    private fun getCurrentYear(): Int {
        val now = kotlin.time.Clock.System.now()
        return now.toLocalDateTime(TimeZone.currentSystemDefault()).year
    }

    fun loadPayslips() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getPayslipsUseCase()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            payslips = result.data
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            sessionExpired = result.isAuthError,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun selectYear(year: Int) {
        _state.update { it.copy(selectedYear = year) }
    }

    fun downloadAndSavePdf(payslip: Payslip) {
        viewModelScope.launch {
            _state.update { it.copy(isDownloading = true, errorMessage = null) }

            when (val result = downloadPayslipUseCase(payslip.id)) {
                is Result.Success -> {
                    val fileName = "Boleta_${payslip.numero}.pdf"
                    val saved = PdfHandler.saveAndOpenPdf(result.data, fileName)
                    _state.update {
                        it.copy(
                            isDownloading = false,
                            downloadedPayslipIds = it.downloadedPayslipIds + payslip.id,
                            errorMessage = if (!saved) "No se pudo abrir el PDF" else null
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isDownloading = false,
                            sessionExpired = result.isAuthError,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun openPayslip(payslip: Payslip) {
        _state.update {
            it.copy(
                selectedPayslip = payslip,
                confirmChecked = false,
                validateMessage = null,
                validateSuccess = null
            )
        }
    }

    fun closePayslip() {
        _state.update {
            it.copy(
                selectedPayslip = null,
                confirmChecked = false,
                validateMessage = null,
                validateSuccess = null
            )
        }
    }

    fun toggleConfirmCheck(checked: Boolean) {
        _state.update { it.copy(confirmChecked = checked) }
    }

    fun validatePayslip(latitud: String? = null, longitud: String? = null) {
        val payslip = _state.value.selectedPayslip ?: return
        if (!_state.value.confirmChecked) return

        viewModelScope.launch {
            _state.update { it.copy(isValidating = true, validateMessage = null) }

            val deviceId = DeviceInfo.getDeviceId()

            when (val result = validatePayslipUseCase(payslip.id, deviceId, latitud, longitud)) {
                is Result.Success -> {
                    val data = result.data
                    _state.update {
                        it.copy(
                            isValidating = false,
                            validateSuccess = data.success,
                            validateMessage = data.message
                        )
                    }
                    if (data.success) {
                        _state.update { current ->
                            current.copy(
                                payslips = current.payslips.map { p ->
                                    if (p.id == payslip.id) p.copy(
                                        validado = true,
                                        fechaValidacion = data.fechaValidacion
                                    ) else p
                                },
                                selectedPayslip = null,
                                confirmChecked = false
                            )
                        }
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isValidating = false,
                            sessionExpired = result.isAuthError,
                            validateMessage = result.message,
                            validateSuccess = false
                        )
                    }
                }
            }
        }
    }

    fun clearState() {
        _state.value = PaymentsState()
    }
}
