package com.konoec.polyworkapp.presentation.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.konoec.polyworkapp.platform.rememberImagePicker
import com.konoec.polyworkapp.presentation.components.PolyworkLoader
import com.konoec.polyworkapp.presentation.theme.PolyworkTheme
import com.konoec.polyworkapp.presentation.ViewModelRegistry

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
                    onTextChange = { viewModel.onJustificationChanged(it) },
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
fun AttendanceStreamItem(item: AttendanceItem, onActionClick: () -> Unit) {
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
        Column(modifier = Modifier.width(45.dp)) {
            Text(
                text = item.date.split("/")[0], // Solo el día
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Light
            )
            Text(
                text = "LUN", // Deberías pasar el día de la semana calculado
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                Text("Resolver", style = MaterialTheme.typography.labelLarge, color = Color(0xFFEF4444))
            }
        }
        // Si es asistencia perfecta, no mostrar nada (queda limpio)
    }
}
@Composable
fun EmptyStateContent(screenPadding: Dp) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = screenPadding)
        ) {
            Icon(
                imageVector = Icons.Rounded.AccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sin registros de asistencia",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No hay asistencias registradas para este periodo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TimelineFormContent(
    item: AttendanceItem,
    justification: String,
    evidenceName: String?,
    isSubmitting: Boolean,
    onTextChange: (String) -> Unit,
    onEvidenceSelected: (fileName: String?, bytes: ByteArray?) -> Unit,
    onSubmit: () -> Unit
) {
    val launchPicker = rememberImagePicker { bytes ->
        if (bytes != null) {
            onEvidenceSelected("evidencia.jpg", bytes)
        } else {
            onEvidenceSelected(null, null)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                Text("Ticket de Incidencia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Ayúdanos a corregir tu registro del ${item.date}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("1. Descripción del problema", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = justification,
            onValueChange = onTextChange,
            placeholder = { Text("Ej: Olvidé marcar mi salida porque...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Notes, null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("2. Evidencia (Opcional)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        val stroke = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f))
        val borderColor = if (evidenceName != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .drawBehind {
                    drawRoundRect(color = borderColor, style = stroke, cornerRadius = CornerRadius(12.dp.toPx()))
                }
                .background(if (evidenceName != null) MaterialTheme.colorScheme.primary.copy(alpha=0.05f) else Color.Transparent, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .clickable { launchPicker() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (evidenceName != null) Icons.Outlined.CheckCircle else Icons.Outlined.CloudUpload,
                    contentDescription = null,
                    tint = if (evidenceName != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = evidenceName ?: "Toca para subir foto",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (evidenceName != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = !isSubmitting && justification.isNotEmpty()
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("ENVIAR SOLICITUD", fontWeight = FontWeight.Bold)
            }
        }
    }
}