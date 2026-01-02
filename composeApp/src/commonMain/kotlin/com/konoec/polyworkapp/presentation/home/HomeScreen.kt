package com.konoec.polyworkapp.presentation.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.WorkHistory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.konoec.polyworkapp.domain.model.ShiftStatus
import com.konoec.polyworkapp.presentation.navigation.Screen
import com.konoec.polyworkapp.presentation.theme.PolyworkTheme
import com.konoec.polyworkapp.presentation.theme.PolyworkWindowClass
import com.konoec.polyworkapp.presentation.util.formatNextShiftTime
import com.konoec.polyworkapp.presentation.util.formatWorkSchedule

@Composable
fun HomeScreen(
    navController: NavController
) {
    val vm: HomeViewModel = viewModel { HomeViewModel() }
    val state by vm.state.collectAsState()

    val scrollState = rememberScrollState()
    val d = PolyworkTheme.dimens
    val wc = PolyworkTheme.windowClass
    var profileMenuExpanded by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var isChangingPassword by remember { mutableStateOf(false) }
    var passwordChangeMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Recargar datos si el estado está vacío (después de logout y nuevo login)
    LaunchedEffect(state.user) {
        if (state.user == null && !state.isLoading && !state.sessionExpired) {
            vm.refreshData()
        }
    }

    // Mostrar mensaje cuando cambia passwordChangeMessage
    LaunchedEffect(passwordChangeMessage) {
        passwordChangeMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            passwordChangeMessage = null
        }
    }

    // Navegar al login si la sesión expiró
    LaunchedEffect(state.sessionExpired) {
        if (state.sessionExpired) {
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // --- DATOS DEL USUARIO ---
    val userName = state.user?.name ?: "Colaborador"
    val userDni = state.user?.dni ?: "--------"

    // Lógica para obtener iniciales (Ej: "Juan Pérez" -> "JP")
    val userInitials = remember(userName) {
        userName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
    }

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
                    .verticalScroll(scrollState)
            ) {

                // --- 1. HEADER (Saludo e Iniciales) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hola, ${userName.split(" ").first()}", // "Hola, Juan"
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "POLYWORK",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // --- BOTÓN DE PERFIL (Avatar con iniciales) ---
                    Box {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { profileMenuExpanded = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userInitials,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // --- MENÚ DESPLEGABLE ---
                        ProfileDropdownMenu(
                            expanded = profileMenuExpanded,
                            onDismissRequest = { profileMenuExpanded = false },
                            collaboratorName = userName,
                            collaboratorDni = userDni,
                            onChangePassword = {
                                showChangePasswordDialog = true
                            },
                            onLogout = {
                                profileMenuExpanded = false
                                vm.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(d.gap))

                // --- 2. HERO CARD ---
                val shift = state.activeShift
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(d.heroHeight)
                        .clip(MaterialTheme.shapes.large)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        )
                ) {
                    // Decoración de fondo (Círculo translúcido)
                    Box(
                        modifier = Modifier
                            .offset(x = 200.dp, y = (-20).dp)
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(d.cardPadding),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.2f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (shift?.status == ShiftStatus.ACTIVE) "EN HORARIO LABORAL" else "ESTADO ACTUAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (shift?.status == ShiftStatus.ACTIVE) "Trabajando" else "Turno Finalizado",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (shift?.status == ShiftStatus.ACTIVE) {
                                formatWorkSchedule(shift.scheduledStartTime, shift.scheduledEndTime)
                            } else {
                                "Próximo ingreso: ${formatNextShiftTime(shift?.nextShiftTime)}"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(d.gap))

                // --- 3. QUICK STATS ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(d.gap)
                ) {
                    val stats = state.stats

                    QuickStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.WorkHistory,
                        value = stats?.diasLaborados?.toString() ?: "--",
                        label = "Días laborados"
                    )

                    QuickStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.AccessTime,
                        value = stats?.let { "${it.puntualidad}%" } ?: "--",
                        label = "Puntualidad"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- 4. SECCIÓN DE ACCESOS ---
                Text(
                    text = "Accesos Directos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(d.gap))

                // Layout adaptativo (Mantenemos tu lógica existente)
                if (wc == PolyworkWindowClass.Expanded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(d.gap)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            HomeModuleItem(
                                title = "Asistencias",
                                desc = "Registro de entradas y salidas",
                                icon = Icons.Outlined.CalendarMonth,
                                color = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    navController.navigate(Screen.Attendance.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                            HomeModuleItem(
                                title = "Boletas",
                                desc = "Próximamente",
                                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                                color = MaterialTheme.colorScheme.error,
                                enabled = false
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            HomeModuleItem(
                                title = "Horario",
                                desc = "Ver programación semanal",
                                icon = Icons.Outlined.Schedule,
                                color = Color(0xFF38BDF8),
                                onClick = {
                                    navController.navigate(Screen.Schedule.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        HomeModuleItem(
                            title = "Asistencias",
                            desc = "Registro de entradas y salidas",
                            icon = Icons.Outlined.CalendarMonth,
                            color = MaterialTheme.colorScheme.primary,
                            onClick = {
                                navController.navigate(Screen.Attendance.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        HomeModuleItem(
                            title = "Horario",
                            desc = "Ver programación semanal",
                            icon = Icons.Outlined.Schedule,
                            color = Color(0xFF38BDF8),
                            onClick = {
                                navController.navigate(Screen.Schedule.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        HomeModuleItem(
                            title = "Boletas",
                            desc = "Próximamente",
                            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                            color = MaterialTheme.colorScheme.error,
                            enabled = false
                        )
                    }
                }
            }
        }
    }

    // Dialog de cambio de contraseña
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = {
                showChangePasswordDialog = false
                isChangingPassword = false
            },
            onChangePassword = { currentPassword, newPassword ->
                isChangingPassword = true
                vm.changePassword(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    onSuccess = { message ->
                        isChangingPassword = false
                        showChangePasswordDialog = false
                        passwordChangeMessage = message
                    },
                    onError = { error ->
                        isChangingPassword = false
                        passwordChangeMessage = error
                    }
                )
            },
            isLoading = isChangingPassword
        )
    }

    // SnackbarHost para mostrar mensajes
    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun QuickStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String
) {
    val d = PolyworkTheme.dimens
    Surface(
        modifier = modifier.height(100.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(d.cardPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
fun HomeModuleItem(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val contentAlpha = if (enabled) 1f else 0.4f

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.98f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "home_item_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (enabled) 1f else 0.5f),
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) color else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                )
            }
            if (enabled) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}