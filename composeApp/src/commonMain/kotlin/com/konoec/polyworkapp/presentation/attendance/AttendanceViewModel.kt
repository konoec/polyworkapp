package com.konoec.polyworkapp.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.konoec.polyworkapp.di.AppModule
import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.usecase.GetAttendanceRecordsUseCase
import com.konoec.polyworkapp.domain.usecase.SubmitJustificationUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.konoec.polyworkapp.domain.model.AttendanceStatus as DomainAttendanceStatus

class AttendanceViewModel(
    private val getAttendanceRecordsUseCase: GetAttendanceRecordsUseCase = AppModule.getAttendanceRecordsUseCase,
    private val submitJustificationUseCase: SubmitJustificationUseCase = AppModule.submitJustificationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AttendanceState())
    val state = _state.asStateFlow()

    private val _effects = Channel<AttendanceEffect>(capacity = Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    // Obtener el mes actual dinámicamente
    private var currentMonthId: Int = getCurrentMonth()

    init {
        loadAttendance()
    }

    private fun getCurrentMonth(): Int {
        val now = kotlin.time.Clock.System.now()
        val localDate = now.toLocalDateTime(TimeZone.currentSystemDefault())
        return localDate.monthNumber
    }

    private fun loadAttendance() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getAttendanceRecordsUseCase(currentMonthId)) {
                is Result.Success -> {
                    val (records, months) = result.data

                    val attendanceItems = records.map { record ->
                        AttendanceItem(
                            id = record.id,
                            date = record.date,
                            scheduledTime = record.scheduledTime,
                            realIn = record.realIn,
                            realOut = record.realOut,
                            status = record.status.toPresentationStatus(),
                            canReport = record.canReport
                        )
                    }

                    val monthTabs = months.map { month ->
                        MonthTab(
                            id = month.id,
                            name = abbreviateMonth(month.name),
                            year = month.year,
                            isSelected = month.id == currentMonthId
                        )
                    }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            attendanceList = attendanceItems,
                            availableMonths = monthTabs
                        )
                    }
                }
                is Result.Error -> {
                    if (result.isAuthError) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                sessionExpired = true,
                                errorMessage = result.message
                            )
                        }
                    } else {
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

    // --- LÓGICA DE SELECCIÓN DE MES ---
    fun selectMonth(selectedId: Int) {
        currentMonthId = selectedId

        // Actualizamos la lista para marcar cual está seleccionado
        val updatedMonths = _state.value.availableMonths.map {
            it.copy(isSelected = it.id == selectedId)
        }

        _state.update { it.copy(availableMonths = updatedMonths) }

        // Recargamos la asistencia con el nuevo mes
        loadAttendance()
    }

    // --- LÓGICA DEL FORMULARIO ---
    fun openReportSheet(item: AttendanceItem) {
        if (!item.canReport) return
        _state.update {
            it.copy(
                selectedItem = item,
                isSheetOpen = true,
                justificationText = "",
                evidenceFileName = null,
                errorMessage = null
            )
        }
    }

    fun closeReportSheet() {
        _state.update { it.copy(isSheetOpen = false, selectedItem = null, errorMessage = null) }
    }

    fun onJustificationChanged(text: String) {
        _state.update { it.copy(justificationText = text, errorMessage = null) }
    }

    fun onUploadEvidenceClick() {
        viewModelScope.launch {
            _effects.send(
                AttendanceEffect.OpenEvidencePicker(
                    allowedMimeTypes = listOf("image/*", "application/pdf")
                )
            )
        }
    }

    fun onEvidenceSelected(fileName: String?) {
        _state.update { it.copy(evidenceFileName = fileName, errorMessage = null) }
    }

    fun submitReport() {
        val currentItem = _state.value.selectedItem
        if (currentItem == null) return

        val justification = _state.value.justificationText.trim()
        if (justification.isEmpty()) {
            _state.update { it.copy(errorMessage = "Ingresa el motivo/explicación.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, errorMessage = null) }

            when (val result = submitJustificationUseCase(
                attendanceId = currentItem.id,
                justification = justification,
                evidence = _state.value.evidenceFileName
            )) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            isSheetOpen = false,
                            selectedItem = null,
                            justificationText = "",
                            evidenceFileName = null,
                            errorMessage = null
                        )
                    }
                    _effects.send(AttendanceEffect.ShowSnackbar(result.data))
                    // Recargar la lista para reflejar cambios
                    loadAttendance()
                }
                is Result.Error -> {
                    if (result.isAuthError) {
                        _state.update {
                            it.copy(
                                isSubmitting = false,
                                isSheetOpen = false,
                                sessionExpired = true,
                                errorMessage = result.message
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isSubmitting = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun DomainAttendanceStatus.toPresentationStatus(): AttendanceStatus {
        return when (this) {
            DomainAttendanceStatus.ASISTENCIA -> AttendanceStatus.ASISTENCIA
            DomainAttendanceStatus.TARDANZA -> AttendanceStatus.TARDANZA
            DomainAttendanceStatus.FALTA -> AttendanceStatus.FALTA
            DomainAttendanceStatus.PROCESO -> AttendanceStatus.PROCESO
        }
    }

    private fun abbreviateMonth(monthName: String): String {
        return when (monthName.lowercase()) {
            "enero" -> "ENE"
            "febrero" -> "FEB"
            "marzo" -> "MAR"
            "abril" -> "ABR"
            "mayo" -> "MAY"
            "junio" -> "JUN"
            "julio" -> "JUL"
            "agosto" -> "AGO"
            "septiembre", "setiembre" -> "SEP"
            "octubre" -> "OCT"
            "noviembre" -> "NOV"
            "diciembre" -> "DIC"
            else -> monthName.take(3).uppercase()
        }
    }
}




