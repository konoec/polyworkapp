package com.konoec.polyworkapp.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.konoec.polyworkapp.di.AppModule
import com.konoec.polyworkapp.domain.model.Result
import com.konoec.polyworkapp.domain.usecase.GetAttendanceRecordsUseCase
import com.konoec.polyworkapp.domain.usecase.GetMotivosJustificacionUseCase
import com.konoec.polyworkapp.domain.usecase.SubmitJustificationUseCase
import com.konoec.polyworkapp.platform.DeviceInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import com.konoec.polyworkapp.domain.model.AttendanceStatus as DomainAttendanceStatus

class AttendanceViewModel(
    private val getAttendanceRecordsUseCase: GetAttendanceRecordsUseCase = AppModule.getAttendanceRecordsUseCase,
    private val getMotivosJustificacionUseCase: GetMotivosJustificacionUseCase = AppModule.getMotivosJustificacionUseCase,
    private val submitJustificationUseCase: SubmitJustificationUseCase = AppModule.submitJustificationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AttendanceState())
    val state = _state.asStateFlow()

    private val _effects = Channel<AttendanceEffect>(capacity = Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var currentMonthId: Int = getCurrentMonth()
    private var currentYear: Int = getCurrentYear()

    init {
        val years = generateYearsList()
        _state.update { it.copy(availableYears = years, selectedYear = currentYear) }
        loadMotivos()
        loadAttendance()
    }

    private fun getCurrentMonth(): Int {
        val now = kotlin.time.Clock.System.now()
        val localDate = now.toLocalDateTime(TimeZone.currentSystemDefault())
        return localDate.month.number
    }

    private fun getCurrentYear(): Int {
        val now = kotlin.time.Clock.System.now()
        val localDate = now.toLocalDateTime(TimeZone.currentSystemDefault())
        return localDate.year
    }

    private fun generateYearsList(): List<Int> {
        val currentYear = getCurrentYear()
        return (2017..currentYear).toList()
    }

    fun loadAttendance() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getAttendanceRecordsUseCase(currentMonthId, currentYear)) {
                is Result.Success -> {
                    val (records, monthsData) = result.data

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

                    // Mapeamos los Tabs del dominio a MonthTab
                    val monthTabs = monthsData.map { tab ->
                        MonthTab(
                            id = tab.id,
                            name = abbreviateMonth(tab.name),
                            year = currentYear.toString(),
                            isSelected = tab.id == currentMonthId
                        )
                    }

                    // Calcular resumen del periodo
                    val summary = calculatePeriodSummary(attendanceItems)

                    _state.update {
                        it.copy(
                            isLoading = false,
                            attendanceList = attendanceItems,
                            availableMonths = monthTabs,
                            periodSummary = summary
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

    fun selectMonth(selectedId: Int) {
        currentMonthId = selectedId
        // Actualizamos visualmente la selección antes de cargar
        val updatedMonths = _state.value.availableMonths.map {
            it.copy(isSelected = it.id == selectedId)
        }
        _state.update { it.copy(availableMonths = updatedMonths) }
        loadAttendance()
    }

    fun selectYear(year: Int) {
        currentYear = year
        _state.update { it.copy(selectedYear = year) }
        loadAttendance()
    }

    fun openReportSheet(item: AttendanceItem) {
        if (!item.canReport) return
        _state.update {
            it.copy(
                selectedItem = item,
                isSheetOpen = true,
                justificationText = "",
                evidenceFileName = null,
                selectedMotivoId = null,
                errorMessage = null
            )
        }
        // Cargar motivos si aún no se han cargado
        if (_state.value.motivosJustificacion.isEmpty()) {
            loadMotivos()
        }
    }

    fun closeReportSheet() {
        _state.update { it.copy(isSheetOpen = false, selectedItem = null, selectedMotivoId = null, errorMessage = null) }
    }

    fun onJustificationChanged(text: String) {
        _state.update { it.copy(justificationText = text, errorMessage = null) }
    }

    fun onMotivoSelected(motivoId: Int?) {
        _state.update { it.copy(selectedMotivoId = motivoId, errorMessage = null) }
    }

    private fun loadMotivos() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMotivos = true) }

            when (val result = getMotivosJustificacionUseCase()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            motivosJustificacion = result.data,
                            isLoadingMotivos = false
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoadingMotivos = false,
                            errorMessage = "No se pudieron cargar los motivos"
                        )
                    }
                }
            }
        }
    }

    fun onEvidenceSelected(fileName: String?, bytes: ByteArray?) {
        _state.update {
            it.copy(
                evidenceFileName = fileName,
                evidenceBytes = bytes,
                errorMessage = null
            )
        }
    }

    fun submitReport() {
        val currentItem = _state.value.selectedItem ?: return
        val justification = _state.value.justificationText.trim()
        val motivoId = _state.value.selectedMotivoId

        // Validar que se haya seleccionado un motivo
        if (motivoId == null) {
            _state.update { it.copy(errorMessage = "Selecciona un motivo de justificación.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, errorMessage = null) }

            val deviceId = DeviceInfo.getDeviceId()
            val imageBytes = _state.value.evidenceBytes
            val fileName = _state.value.evidenceFileName

            when (val result = submitJustificationUseCase(
                attendanceId = currentItem.id,
                description = justification,
                deviceId = deviceId,
                imageBytes = imageBytes,
                fileName = fileName,
                motivoId = motivoId
            )) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            isSheetOpen = false,
                            selectedItem = null,
                            justificationText = "",
                            evidenceFileName = null,
                            evidenceBytes = null,
                            selectedMotivoId = null,
                            errorMessage = null
                        )
                    }
                    _effects.send(AttendanceEffect.ShowSnackbar(result.data))
                    loadAttendance()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            sessionExpired = result.isAuthError,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun reloadIfNeeded() {
        if (_state.value.attendanceList.isEmpty() && !_state.value.isLoading) {
            loadAttendance()
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

    private fun calculatePeriodSummary(items: List<AttendanceItem>): PeriodSummary {
        var asistencias = 0
        var tardanzas = 0
        var faltas = 0
        var enProceso = 0

        items.forEach { item ->
            when (item.status) {
                AttendanceStatus.ASISTENCIA -> asistencias++
                AttendanceStatus.TARDANZA -> tardanzas++
                AttendanceStatus.FALTA -> faltas++
                AttendanceStatus.PROCESO -> enProceso++
            }
        }

        return PeriodSummary(
            totalDays = items.size,
            asistencias = asistencias,
            tardanzas = tardanzas,
            faltas = faltas,
            enProceso = enProceso
        )
    }

    /**
     * Limpia el estado del ViewModel.
     * Se debe llamar al hacer logout.
     */
    fun clearState() {
        _state.value = AttendanceState()
        currentMonthId = getCurrentMonth()
        currentYear = getCurrentYear()
    }
}

