package com.konoec.polyworkapp.presentation.attendance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
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
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = d.maxContentWidth),
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

// ==========================================
// --- UI COMPONENTS ---
// ==========================================

@Composable
fun AttendanceTopBar(
    selectedYear: Int,
    availableYears: List<Int>,
    availableMonths: List<MonthTab>,
    onYearSelected: (Int) -> Unit,
    onMonthSelected: (Int) -> Unit,
    screenPadding: Dp
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // Row 1: Title & Year
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = screenPadding, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mis Asistencias",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Historial administrativo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                CompactYearSelector(
                    years = availableYears,
                    selectedYear = selectedYear,
                    onYearSelected = onYearSelected
                )
            }

            // Row 2: Month Chips
            MonthScrollableSelector(
                months = availableMonths,
                onMonthSelected = onMonthSelected,
                screenPadding = screenPadding
            )
        }
    }
}

@Composable
fun CompactYearSelector(
    years: List<Int>,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        AssistChip(
            onClick = { expanded = true },
            label = {
                Text(
                    text = selectedYear.toString(),
                    fontWeight = FontWeight.Bold
                )
            },
            trailingIcon = {
                Icon(
                    if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                labelColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(50)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = year.toString(),
                            fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal,
                            color = if (year == selectedYear) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onYearSelected(year)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MonthScrollableSelector(
    months: List<MonthTab>,
    onMonthSelected: (Int) -> Unit,
    screenPadding: Dp
) {
    val listState = rememberLazyListState()

    LaunchedEffect(months) {
        val selectedIndex = months.indexOfFirst { it.isSelected }
        if (selectedIndex >= 0) {
            listState.animateScrollToItem(
                index = selectedIndex,
                scrollOffset = -100
            )
        }
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = screenPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(months) { month ->
            val isSelected = month.isSelected

            Surface(
                onClick = { onMonthSelected(month.id) },
                shape = RoundedCornerShape(50),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.height(36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = month.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
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
fun AttendanceStreamItem(item: AttendanceItem, onActionClick: () -> Unit) {
    val statusColor = item.status.color
    val hasIssue = item.canReport

    val (day, monthAbbr, weekDay) = remember(item.date) {
        val parts = item.date.split("/")
        if (parts.size >= 3) {
            val dayStr = parts[0]
            val monthNum = parts[1].toIntOrNull() ?: 1
            val yearNum = parts[2].toIntOrNull() ?: 2025

            val monthStr = when (monthNum) {
                1 -> "ENE" 2 -> "FEB" 3 -> "MAR" 4 -> "ABR" 5 -> "MAY" 6 -> "JUN"
                7 -> "JUL" 8 -> "AGO" 9 -> "SEP" 10 -> "OCT" 11 -> "NOV" 12 -> "DIC"
                else -> "???"
            }

            val weekDayStr = try {
                val date = kotlinx.datetime.LocalDate(yearNum, monthNum, dayStr.toInt())
                when (date.dayOfWeek) {
                    kotlinx.datetime.DayOfWeek.MONDAY -> "LUN"
                    kotlinx.datetime.DayOfWeek.TUESDAY -> "MAR"
                    kotlinx.datetime.DayOfWeek.WEDNESDAY -> "MIÉ"
                    kotlinx.datetime.DayOfWeek.THURSDAY -> "JUE"
                    kotlinx.datetime.DayOfWeek.FRIDAY -> "VIE"
                    kotlinx.datetime.DayOfWeek.SATURDAY -> "SÁB"
                    kotlinx.datetime.DayOfWeek.SUNDAY -> "DOM"
                }
            } catch (_: Exception) { "" }

            Triple(dayStr, monthStr, weekDayStr)
        } else {
            Triple("??", "???", "")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(50.dp).padding(top = 0.dp)
        ) {
            if (weekDay.isNotEmpty()) {
                Text(
                    text = weekDay,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Text(
                text = day,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = monthAbbr,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(12.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .border(2.dp, statusColor, CircleShape)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(
                        if (hasIssue) statusColor.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    )
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 40.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Turno: ${item.scheduledTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.realIn ?: "--:--",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (item.realIn == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp).size(16.dp)
                )

                Text(
                    text = item.realOut ?: "--:--",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (item.realOut == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.status.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (hasIssue) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(6.dp),
                        onClick = onActionClick
                    ) {
                        Text(
                            text = "Justificar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
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