package com.konoec.polyworkapp.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Attendance : Screen("attendance", "Asistencias", Icons.Default.DateRange)
    object Schedule : Screen("schedule", "Horario", Icons.AutoMirrored.Filled.List)
    object Payments : Screen("payments", "Boletas", Icons.Default.Person)

    object ReportIssue : Screen("report_issue")
    object Login : Screen("login")

    companion object {
        val bottomNavItems = listOf(Home, Attendance, Schedule, Payments)
    }
}