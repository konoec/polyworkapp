package com.konoec.polyworkapp.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Workspaces // Icono de marca simulado
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.konoec.polyworkapp.presentation.theme.PolyworkTheme
import androidx.compose.ui.graphics.vector.ImageVector
import com.konoec.polyworkapp.AppVersion

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
) {
    val vm: LoginViewModel = viewModel { LoginViewModel() }
    val state: LoginState by vm.state.collectAsState()
    val focusManager = LocalFocusManager.current

    val d = PolyworkTheme.dimens
    val wc = PolyworkTheme.windowClass

    val maxWidth = d.maxContentWidth
    val horizontalPadding = d.screenPadding

    val logoSize = when (wc) {
        com.konoec.polyworkapp.presentation.theme.PolyworkWindowClass.Compact -> 72.dp
        com.konoec.polyworkapp.presentation.theme.PolyworkWindowClass.Medium -> 80.dp
        com.konoec.polyworkapp.presentation.theme.PolyworkWindowClass.Expanded -> 92.dp
    }

    val formTopSpacing = when (wc) {
        com.konoec.polyworkapp.presentation.theme.PolyworkWindowClass.Compact -> 32.dp
        com.konoec.polyworkapp.presentation.theme.PolyworkWindowClass.Medium -> 40.dp
        com.konoec.polyworkapp.presentation.theme.PolyworkWindowClass.Expanded -> 48.dp
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 1. FONDO AMBIENTAL (Glow Effect)
            // Un degradado radial rojo en la parte superior para dar profundidad
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            // 2. CONTENIDO SCROLLEABLE
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = maxWidth)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // --- LOGO Y MARCA ---
                Box(
                    modifier = Modifier
                        .size(logoSize)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Workspaces, // Icono de la App
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(logoSize / 2)
                    )
                }

                Spacer(modifier = Modifier.height(d.gap))

                Text(
                    text = "POLYWORK",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Portal del Colaborador",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(formTopSpacing))

                // --- FORMULARIO ---

                // Campo DNI
                PolyworkLoginInput(
                    value = state.dni,
                    onValueChange = vm::onDniChange,
                    label = "DNI",
                    icon = Icons.Outlined.Person,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Password
                PolyworkLoginInput(
                    value = state.password,
                    onValueChange = vm::onPasswordChange,
                    label = "Contraseña",
                    icon = Icons.Outlined.Lock,
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    onAction = {
                        focusManager.clearFocus()
                        vm.login(onLoginSuccess)
                    }
                )

                // Mensaje de Error
                if (state.errorMessage != null) {
                    Spacer(modifier = Modifier.height(d.gap))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(formTopSpacing.coerceAtMost(32.dp)))

                // Botón Principal
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        vm.login(onLoginSuccess)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "INICIAR SESIÓN",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(formTopSpacing.coerceAtMost(32.dp)))

                // --- FOOTER ---
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "¿Problemas para ingresar?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Contactar a Recursos Humanos",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp) // Aumentar área táctil
                    )

                    // Versión de la app
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Versión ${AppVersion.VERSION_NAME}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }

                // Espacio extra para scroll en pantallas pequeñas
                Spacer(modifier = Modifier.height(d.gap))
            }
        }
    }
}

// --- COMPONENTE REUTILIZABLE PARA INPUTS ---
@Composable
fun PolyworkLoginInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    imeAction: ImeAction = ImeAction.Default,
    onAction: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { onAction() },
            onNext = { /* El sistema maneja el foco al siguiente automáticamente */ }
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    )
}