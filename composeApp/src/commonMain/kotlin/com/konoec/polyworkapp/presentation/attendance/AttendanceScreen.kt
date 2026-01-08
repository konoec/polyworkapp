package com.konoec.polyworkapp.presentation.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.konoec.polyworkapp.platform.FilePickerResult
import com.konoec.polyworkapp.platform.rememberImagePicker
import com.konoec.polyworkapp.presentation.components.PolyworkLoader
import com.konoec.polyworkapp.presentation.theme.PolyworkTheme
import com.konoec.polyworkapp.presentation.ViewModelRegistry
import kotlinx.coroutines.launch
import kotlinx.datetime.*

/**
 * Parsea una fecha en formato "DD/MM/YYYY" y retorna el día de la semana abreviado en español
 */
private fun getDayOfWeek(dateString: String): String {
    return try {
        val parts = dateString.split("/")
        if (parts.size != 3) return "---"

        val day = parts[0].toIntOrNull() ?: return "---"
        val month = parts[1].toIntOrNull() ?: return "---"
        val year = parts[2].toIntOrNull() ?: return "---"

        val localDate = LocalDate(year, month, day)
        val dayOfWeek = localDate.dayOfWeek

        when (dayOfWeek) {
            DayOfWeek.MONDAY -> "LUN"
            DayOfWeek.TUESDAY -> "MAR"
            DayOfWeek.WEDNESDAY -> "MIE"
            DayOfWeek.THURSDAY -> "JUE"
            DayOfWeek.FRIDAY -> "VIE"
            DayOfWeek.SATURDAY -> "SAB"
            DayOfWeek.SUNDAY -> "DOM"
        }
    } catch (e: Exception) {
        "---"
    }
}

/**
 * Parsea una fecha en formato "DD/MM/YYYY" y retorna el mes abreviado en español
 */
private fun getMonthAbbreviation(dateString: String): String {
    return try {
        val parts = dateString.split("/")
        if (parts.size != 3) return ""

        val month = parts[1].toIntOrNull() ?: return ""

        when (month) {
            1 -> "ENE"
            2 -> "FEB"
            3 -> "MAR"
            4 -> "ABR"
            5 -> "MAY"
            6 -> "JUN"
            7 -> "JUL"
            8 -> "AGO"
            9 -> "SEP"
            10 -> "OCT"
            11 -> "NOV"
            12 -> "DIC"
            else -> ""
        }
    } catch (e: Exception) {
        ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen() {
    val viewModel: AttendanceViewModel = viewModel { AttendanceViewModel() }
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(viewModel) {
        ViewModelRegistry.registerAttendanceViewModel(viewModel)
    }

    LaunchedEffect(state.attendanceList) {
        viewModel.reloadIfNeeded()
    }

    val d = PolyworkTheme.dimens
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AttendanceEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AttendanceTopBar(
                selectedYear = state.selectedYear,
                availableYears = state.availableYears,
                availableMonths = state.availableMonths,
                onYearSelected = { viewModel.selectYear(it) },
                onMonthSelected = { viewModel.selectMonth(it) },
                screenPadding = d.screenPadding
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            PolyworkLoader()
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.TopCenter
            ) {
                if (state.attendanceList.isEmpty()) {
                    EmptyStateContent(d.screenPadding)
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = d.maxContentWidth)
                    ) {
                        // Resumen del periodo
                        PeriodSummaryCard(
                            summary = state.periodSummary,
                            screenPadding = d.screenPadding
                        )

                        // Lista de asistencias
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = d.screenPadding,
                                end = d.screenPadding,
                                top = 16.dp,
                                bottom = 80.dp
                            )
                        ) {
                            items(state.attendanceList) { item ->
                                AttendanceStreamItem(
                                    item = item,
                                    onActionClick = { viewModel.openReportSheet(item) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.isSheetOpen && state.selectedItem != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeReportSheet() },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                TimelineFormContent(
                    item = state.selectedItem!!,
                    justification = state.justificationText,
                    evidenceName = state.evidenceFileName,
                    isSubmitting = state.isSubmitting,
                    motivosDisponibles = state.motivosJustificacion,
                    selectedMotivoId = state.selectedMotivoId,
                    isLoadingMotivos = state.isLoadingMotivos,
                    errorMessage = state.errorMessage,
                    onTextChange = { viewModel.onJustificationChanged(it) },
                    onMotivoSelected = { viewModel.onMotivoSelected(it) },
                    onEvidenceSelected = { fileName, bytes ->
                        viewModel.onEvidenceSelected(fileName, bytes)
                    },
                    onSubmit = { viewModel.submitReport() }
                )
            }
        }
    }
}

@Composable
fun AttendanceTopBar(
    selectedYear: Int,
    availableYears: List<Int>,
    availableMonths: List<MonthTab>,
    onYearSelected: (Int) -> Unit,
    onMonthSelected: (Int) -> Unit,
    screenPadding: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Row de Título y Selector de Año
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = screenPadding, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Mis Asistencias",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )

            // Selector Real
            var expanded by remember { mutableStateOf(false) }
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expanded = true }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    availableYears.forEach { year ->
                        DropdownMenuItem(
                            text = { Text(year.toString()) },
                            onClick = {
                                onYearSelected(year)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Selector de meses tipo "Tab" minimalista
        LazyRow(
            contentPadding = PaddingValues(horizontal = screenPadding),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(availableMonths) { month ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onMonthSelected(month.id) }
                ) {
                    Text(
                        text = month.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (month.isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (month.isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (month.isSelected) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(4.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodSummaryCard(
    summary: PeriodSummary,
    screenPadding: Dp
) {
    // Diseño tipo "Dashboard de una sola línea"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = screenPadding, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val stats = listOf(
            Triple(summary.asistencias, "Asistió", AttendanceStatus.ASISTENCIA.color),
            Triple(summary.tardanzas, "Tarde", AttendanceStatus.TARDANZA.color),
            Triple(summary.faltas, "Faltas", AttendanceStatus.FALTA.color),
            Triple(summary.enProceso, "En Proceso", Color(0xFF60A5FA))
        )

        stats.forEach { (count, label, color) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyStateContent(screenPadding: Dp) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(screenPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📋",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sin registros",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "No hay asistencias en este periodo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AttendanceStreamItem(
    item: AttendanceItem,
    onActionClick: () -> Unit
) {
    // Determinar el color según el estado
    val statusColor = when (item.status) {
        AttendanceStatus.ASISTENCIA -> Color(0xFF4ADE80) // Verde
        AttendanceStatus.TARDANZA -> Color(0xFFFACC15)   // Amarillo
        AttendanceStatus.FALTA -> Color(0xFFEF4444)      // Rojo
        AttendanceStatus.PROCESO -> Color(0xFF60A5FA)    // Azul
    }

    // Mostrar "Resolver" solo para tardanza, falta o proceso
    val shouldShowResolve = item.canReport && (
        item.status == AttendanceStatus.TARDANZA ||
        item.status == AttendanceStatus.FALTA ||
        item.status == AttendanceStatus.PROCESO
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Fecha Minimalista
        Column(
            modifier = Modifier.width(52.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.date.split("/")[0], // Solo el día
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Light
            )
            Text(
                text = getDayOfWeek(item.date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = getMonthAbbreviation(item.date),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.75
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        // Línea de tiempo sutil con color según estado
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(2.dp, 40.dp)
                .background(statusColor)
        )

        // Información de marcación
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.realIn ?: "--"} — ${item.realOut ?: "--"}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (shouldShowResolve) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(6.dp).background(statusColor, CircleShape))
                }
            }
            Text(
                text = "Horario: ${item.scheduledTime}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Estado o botón de acción
        if (shouldShowResolve) {
            TextButton(onClick = onActionClick) {
                Text("Justificar", style = MaterialTheme.typography.labelLarge, color = Color(0xFFEF4444))
            }
        }
        // Si es asistencia perfecta, no mostrar nada (queda limpio)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineFormContent(
    item: AttendanceItem,
    justification: String,
    evidenceName: String?,
    isSubmitting: Boolean,
    motivosDisponibles: List<com.konoec.polyworkapp.domain.repository.MotivoJustificacion>,
    selectedMotivoId: Int?,
    isLoadingMotivos: Boolean,
    errorMessage: String?,
    onTextChange: (String) -> Unit,
    onMotivoSelected: (Int?) -> Unit,
    onEvidenceSelected: (fileName: String?, bytes: ByteArray?) -> Unit,
    onSubmit: () -> Unit
) {
    // Estados para el Selector de Motivo (Dropdown)
    var expanded by remember { mutableStateOf(false) }

    // Encontrar el motivo seleccionado actual
    val selectedMotivoText = motivosDisponibles.find { it.id == selectedMotivoId }?.descripcion ?: ""

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val launchPicker = rememberImagePicker { result ->
        when (result) {
            is FilePickerResult.Success -> {
                onEvidenceSelected(result.fileName, result.bytes)
            }
            is FilePickerResult.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = result.message,
                        duration = SnackbarDuration.Short
                    )
                }
                onEvidenceSelected(null, null)
            }
            is FilePickerResult.Cancelled -> {
                // Usuario canceló, no hacer nada
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState) // Permite scroll si el contenido crece
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        // --- CABECERA ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Warning, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "Ticket de Justificación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Registro del ${item.date}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 1. SELECTOR DE MOTIVO (DINÁMICO DESDE BACKEND) ---
        Text(
            "1. Motivo de la incidencia",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoadingMotivos) {
            // Mostrar un indicador de carga mientras se obtienen los motivos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        } else {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedMotivoText,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Selecciona un motivo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    leadingIcon = { Icon(Icons.AutoMirrored.Rounded.List, null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = errorMessage != null && selectedMotivoId == null
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    motivosDisponibles.forEach { motivo ->
                        DropdownMenuItem(
                            text = { Text(motivo.descripcion, style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                onMotivoSelected(motivo.id)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }

        // Mostrar mensaje de error si existe
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. DESCRIPCIÓN (OPCIONAL) ---
        Text(
            "2. Detalles adicionales (Opcional)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = justification,
            onValueChange = onTextChange,
            placeholder = { Text("Describe brevemente lo ocurrido...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Notes, null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- 3. EVIDENCIA (OPCIONAL) ---
        Text(
            "3. Evidencia (Opcional)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        val stroke = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f))
        val borderColor = if (evidenceName != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .drawBehind {
                    drawRoundRect(color = borderColor, style = stroke, cornerRadius = CornerRadius(12.dp.toPx()))
                }
                .background(
                    if (evidenceName != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    else Color.Transparent,
                    RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .clickable { launchPicker() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = if (evidenceName != null) Icons.Outlined.CheckCircle else Icons.Outlined.CloudUpload,
                    contentDescription = null,
                    tint = if (evidenceName != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = evidenceName ?: "Toca para subir una foto o PDF",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (evidenceName != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- BOTÓN DE ENVÍO ---
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            // El botón se habilita solo si se seleccionó un motivo
            enabled = !isSubmitting && selectedMotivoId != null
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp
                )
            } else {
                Text("ENVIAR SOLICITUD", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
            }
        }
    }

        // Snackbar para mostrar errores del selector de archivos
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}