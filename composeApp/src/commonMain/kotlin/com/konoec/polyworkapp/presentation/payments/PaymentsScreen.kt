package com.konoec.polyworkapp.presentation.payments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.konoec.polyworkapp.domain.model.Payslip
import com.konoec.polyworkapp.presentation.ViewModelRegistry
import com.konoec.polyworkapp.presentation.components.PolyworkLoader
import com.konoec.polyworkapp.presentation.theme.PolyworkTheme

private val SignedGreen = Color(0xFF22C55E)
private val PendingOrange = Color(0xFFE11D48)

@Composable
fun PaymentsScreen() {
    val vm: PaymentsViewModel = viewModel { PaymentsViewModel() }
    ViewModelRegistry.registerPaymentsViewModel(vm)
    val state by vm.state.collectAsState()
    val d = PolyworkTheme.dimens

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = d.maxContentWidth)
                    .padding(d.screenPadding)
            ) {
                PayslipHeader(
                    userName = state.payslips.firstOrNull()?.trabajador?.split(",")
                        ?.lastOrNull()?.trim()?.split(" ")?.firstOrNull() ?: "",
                    selectedYear = state.selectedYear,
                    availableYears = state.availableYears,
                    onYearSelected = vm::selectYear
                )

                Spacer(modifier = Modifier.height(d.gap))

                when {
                    state.isLoading -> PolyworkLoader()
                    state.errorMessage != null && state.payslips.isEmpty() -> {
                        ErrorContent(
                            message = state.errorMessage!!,
                            onRetry = vm::loadPayslips
                        )
                    }
                    state.filteredPayslips.isEmpty() -> EmptyContent()
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(d.gap)
                        ) {
                            items(
                                items = state.filteredPayslips,
                                key = { it.id }
                            ) { payslip ->
                                PayslipCard(
                                    payslip = payslip,
                                    onCardClick = { vm.openPayslip(payslip) },
                                    onDownload = { vm.downloadAndSavePdf(payslip) },
                                    onSign = { vm.openPayslip(payslip) }
                                )
                            }
                        }
                    }
                }
            }

            // --- OVERLAY BOTTOM SHEET ---
            AnimatedVisibility(
                visible = state.selectedPayslip != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                state.selectedPayslip?.let { payslip ->
                    PayslipOverlay(
                        payslip = payslip,
                        isDownloading = state.isDownloading,
                        wasDownloaded = payslip.id in state.downloadedPayslipIds,
                        isValidating = state.isValidating,
                        confirmChecked = state.confirmChecked,
                        validateMessage = state.validateMessage,
                        validateSuccess = state.validateSuccess,
                        onClose = vm::closePayslip,
                        onDownload = { vm.downloadAndSavePdf(payslip) },
                        onToggleConfirm = vm::toggleConfirmCheck,
                        onConfirmValidate = { vm.validatePayslip() }
                    )
                }
            }
        }
    }
}

// ================================
// HEADER
// ================================
@Composable
private fun PayslipHeader(
    userName: String,
    selectedYear: Int,
    availableYears: List<Int>,
    onYearSelected: (Int) -> Unit
) {
    var yearDropdownExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (userName.isNotBlank()) {
                Text(
                    text = "Hola, $userName",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Mis Boletas",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Historial de Pagos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box {
            TextButton(onClick = { yearDropdownExpanded = true }) {
                Text(
                    text = "$selectedYear",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " ▾",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            DropdownMenu(
                expanded = yearDropdownExpanded,
                onDismissRequest = { yearDropdownExpanded = false }
            ) {
                availableYears.forEach { year ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "$year",
                                fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onYearSelected(year)
                            yearDropdownExpanded = false
                        }
                    )
                }
            }
        }
    }
}

// ================================
// PAYSLIP CARD
// ================================
@Composable
private fun PayslipCard(
    payslip: Payslip,
    onCardClick: () -> Unit,
    onDownload: () -> Unit,
    onSign: () -> Unit
) {
    val isSigned = payslip.validado

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onCardClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PictureAsPdf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getMonthLabel(payslip.mes, payslip.periodo),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = payslip.periodoLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = if (isSigned) SignedGreen.copy(alpha = 0.15f) else PendingOrange.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isSigned) "VALIDADO" else "PENDIENTE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSigned) SignedGreen else PendingOrange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (isSigned) {
                    OutlinedButton(
                        onClick = onDownload,
                        modifier = Modifier.height(32.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Outlined.Download, null, Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("DESCARGAR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onSign,
                        modifier = Modifier.height(32.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Outlined.Draw, null, Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("VALIDAR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ================================
// OVERLAY (Bottom Sheet style)
// ================================
@Composable
private fun PayslipOverlay(
    payslip: Payslip,
    isDownloading: Boolean,
    wasDownloaded: Boolean,
    isValidating: Boolean,
    confirmChecked: Boolean,
    validateMessage: String?,
    validateSuccess: Boolean?,
    onClose: () -> Unit,
    onDownload: () -> Unit,
    onToggleConfirm: (Boolean) -> Unit,
    onConfirmValidate: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onClose),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false, onClick = {}),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Boleta de Pago",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Área visual del PDF - clickeable para descargar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !isDownloading, onClick = onDownload),
                    color = Color(0xFF1A1A2E),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDownloading) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Descargando boleta...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.PictureAsPdf,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = getMonthLabel(payslip.mes, payslip.periodo),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = payslip.numero,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Outlined.Download, null,
                                            Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (wasDownloaded) "Descargar de nuevo" else "Toca para descargar",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Aviso de que debe descargar primero
                if (!payslip.validado && !wasDownloaded) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = PendingOrange.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = PendingOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Descarga y revisa tu boleta antes de validarla",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = PendingOrange
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (wasDownloaded && !payslip.validado) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SignedGreen.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = SignedGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Boleta descargada — ahora puedes validarla",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = SignedGreen
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Resultado de validación
                if (validateMessage != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = if (validateSuccess == true) SignedGreen.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (validateSuccess == true) Icons.Outlined.CheckCircle
                                else Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = if (validateSuccess == true) SignedGreen
                                else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = validateMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Sección de validación (solo si pendiente)
                if (!payslip.validado) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = wasDownloaded) {
                                onToggleConfirm(!confirmChecked)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = confirmChecked,
                            onCheckedChange = onToggleConfirm,
                            enabled = wasDownloaded,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                checkmarkColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        Text(
                            text = "He revisado mi boleta, acepto la recepción y confirmo los datos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (wasDownloaded) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onConfirmValidate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = wasDownloaded && confirmChecked && !isValidating && validateSuccess != true,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        if (isValidating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Outlined.Draw, null, Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "VALIDAR BOLETA",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Boleta ya validada
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SignedGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = SignedGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Boleta validada",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SignedGreen
                                )
                                if (payslip.fechaValidacion != null) {
                                    Text(
                                        text = "Validada el ${payslip.fechaValidacion}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================================
// EMPTY / ERROR STATES
// ================================
@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.PictureAsPdf,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay boletas disponibles",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "para el período seleccionado",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

// ================================
// UTILITIES
// ================================
private fun getMonthLabel(mes: Int, periodo: Int): String {
    val monthName = when (mes) {
        1 -> "ENERO"
        2 -> "FEBRERO"
        3 -> "MARZO"
        4 -> "ABRIL"
        5 -> "MAYO"
        6 -> "JUNIO"
        7 -> "JULIO"
        8 -> "AGOSTO"
        9 -> "SEPTIEMBRE"
        10 -> "OCTUBRE"
        11 -> "NOVIEMBRE"
        12 -> "DICIEMBRE"
        else -> "MES $mes"
    }
    return "$monthName $periodo"
}

private fun formatFileSize(bytes: Int): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> {
            val mb = bytes / (1024.0 * 1024.0)
            val rounded = (mb * 10).toInt() / 10.0
            "$rounded MB"
        }
    }
}