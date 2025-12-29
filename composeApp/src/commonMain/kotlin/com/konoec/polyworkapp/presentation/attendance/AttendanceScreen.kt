package com.konoec.polyworkapp.presentation.attendance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.rounded.AccessTime
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.konoec.polyworkapp.presentation.components.PolyworkLoader
import com.konoec.polyworkapp.presentation.theme.PolyworkTheme
import kotlinx.datetime.toLocalDateTime

// --- PANTALLA PRINCIPAL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen() {
    val viewModel: AttendanceViewModel = viewModel { AttendanceViewModel() }
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val d = PolyworkTheme.dimens

    // Obtener año actual
    val currentYear = remember {
        val now = kotlin.time.Clock.System.now()
        now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).year
    }

    // Host para Snackbars
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AttendanceEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is AttendanceEffect.OpenEvidencePicker -> {
                    viewModel.onEvidenceSelected("evidencia_adjunta.pdf")
                    snackbarHostState.showSnackbar("Archivo simulado adjuntado")
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = d.maxContentWidth)
                        .padding(top = d.screenPadding, bottom = 10.dp)
                ) {
                    Text(
                        text = "Mi Cronología",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = d.screenPadding)
                    )
                    Text(
                        text = "Registro mensual del periodo $currentYear",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = d.screenPadding)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = d.screenPadding),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items(state.availableMonths) { month ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable(
                                    interactionSource = null,
                                    indication = null
                                ) { viewModel.selectMonth(month.id) }
                            ) {
                                Text(
                                    text = month.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (month.isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (month.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (month.isSelected) {
                                    Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                                } else {
                                    Box(modifier = Modifier.size(4.dp))
                                }
                            }
                        }
                    }
                }
            }
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
                    // Mensaje cuando no hay asistencias
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = d.screenPadding)
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
                                text = "No hay asistencias registradas para este mes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = d.maxContentWidth),
                        contentPadding = PaddingValues(
                            start = d.screenPadding,
                            end = d.screenPadding,
                            top = 20.dp,
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
                // AQUÍ LLAMAMOS AL FORMULARIO QUE FALTABA
                TimelineFormContent(
                    item = state.selectedItem!!,
                    justification = state.justificationText,
                    evidenceName = state.evidenceFileName,
                    isSubmitting = state.isSubmitting,
                    onTextChange = { viewModel.onJustificationChanged(it) },
                    onUploadClick = { viewModel.onUploadEvidenceClick() },
                    onSubmit = { viewModel.submitReport() }
                )
            }
        }
    }
}

// --- ITEM DE LISTA (TIMELINE STREAM) ---
@Composable
fun AttendanceStreamItem(item: AttendanceItem, onActionClick: () -> Unit) {
    val statusColor = item.status.color
    val hasIssue = item.canReport

    // Formatear fecha: "31/12/2025" -> día "31" y mes "DIC"
    val (day, monthAbbr) = remember(item.date) {
        val parts = item.date.split("/")
        if (parts.size >= 2) {
            val dayStr = parts[0]
            val monthNum = parts[1].toIntOrNull() ?: 1
            val monthStr = when (monthNum) {
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
                else -> "???"
            }
            Pair(dayStr, monthStr)
        } else {
            Pair("??", "???")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // 1. FECHA
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(50.dp).padding(top = 0.dp)
        ) {
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

        // 2. LÍNEA
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

        // 3. CONTENIDO
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

// --- FORMULARIO WIZARD (ESTO ERA LO QUE FALTABA) ---
@Composable
fun TimelineFormContent(
    item: AttendanceItem,
    justification: String,
    evidenceName: String?,
    isSubmitting: Boolean,
    onTextChange: (String) -> Unit,
    onUploadClick: () -> Unit,
    onSubmit: () -> Unit
) {
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
                .clickable { onUploadClick() },
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