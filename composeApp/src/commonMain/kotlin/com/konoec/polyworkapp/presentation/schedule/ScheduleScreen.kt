package com.konoec.polyworkapp.presentation.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.konoec.polyworkapp.presentation.components.PolyworkLoader
import com.konoec.polyworkapp.presentation.theme.PolyworkTheme
import com.konoec.polyworkapp.presentation.util.extractDayFromDate
import com.konoec.polyworkapp.presentation.util.isToday
import com.konoec.polyworkapp.presentation.ViewModelRegistry

@Composable
fun ScheduleScreen() {
    val viewModel: ScheduleViewModel = viewModel { ScheduleViewModel() }
    val state by viewModel.state.collectAsState()

    // Registrar el ViewModel para que pueda ser limpiado al hacer logout
    LaunchedEffect(viewModel) {
        ViewModelRegistry.registerScheduleViewModel(viewModel)
    }

    // Recargar datos si el estado está vacío (después de logout y nuevo login)
    LaunchedEffect(state.shifts) {
        viewModel.reloadIfNeeded()
    }

    val d = PolyworkTheme.dimens

    if (state.isLoading) {
        PolyworkLoader()
    } else {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = d.maxContentWidth),
                    contentPadding = PaddingValues(
                        top = d.screenPadding,
                        bottom = 40.dp,
                        start = d.screenPadding,
                        end = d.screenPadding
                    )
                ) {
                    // Header
                    item {
                        Column(modifier = Modifier.padding(bottom = 32.dp)) {
                            Text(
                                text = "Mi Agenda",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Programación semanal",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    itemsIndexed(state.shifts) { index, shift ->
                        TimelineItemRow(
                            shift = shift,
                            isLast = index == state.shifts.lastIndex,
                            isToday = isToday(shift.date)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineItemRow(shift: ScheduleItem, isLast: Boolean, isToday: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // Vital para la línea vertical
    ) {
        // --- 1. COLUMNA FECHA ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(50.dp)
        ) {
            Text(
                text = extractDayFromDate(shift.date), // "24" desde "24/12/2025"
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = if(isToday) FontWeight.Black else FontWeight.Bold,
                color = if(isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = shift.day.take(3).uppercase(), // "LUN"
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if(isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // --- 2. COLUMNA LÍNEA DE TIEMPO ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            // El Nodo (Anillo)
            Box(
                modifier = Modifier
                    .padding(top = 6.dp) // Alinear ópticamente con el número de fecha
                    .size(16.dp)
                    .background(MaterialTheme.colorScheme.background, CircleShape)
                    .border(
                        width = 3.dp,
                        color = if(isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
            )

            // La Línea
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                )
            }
        }

        // --- 3. COLUMNA TARJETA ---
        Box(modifier = Modifier.weight(1f).padding(bottom = 24.dp)) {
            ScheduleRichCard(shift, isToday)
        }
    }
}

@Composable
fun ScheduleRichCard(shift: ScheduleItem, highlight: Boolean) {
    val isNightShift = shift.shiftType.contains("Noche", ignoreCase = true)
    val shiftIcon = if(isNightShift) Icons.Rounded.DarkMode else Icons.Rounded.WbSunny
    val shiftName = shift.shiftType // "Turno Mañana", "Turno Noche", "Descanso"

    val containerColor = if (highlight)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    val borderColor = if (highlight)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    else
        Color.Transparent

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header de la tarjeta (Tipo de turno + Duración)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chip del Turno
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = shiftIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = shiftName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Duración de la API
                Surface(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = shift.duration, // "9 hrs" viene de la API
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // La Hora Grande
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = shift.time, // "08:00 - 17:00"
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}