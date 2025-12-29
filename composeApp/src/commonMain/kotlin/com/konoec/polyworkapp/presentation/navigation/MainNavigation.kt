package com.konoec.polyworkapp.presentation.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.konoec.polyworkapp.presentation.attendance.AttendanceScreen
import com.konoec.polyworkapp.presentation.home.HomeScreen
import com.konoec.polyworkapp.presentation.schedule.ScheduleScreen
import com.konoec.polyworkapp.presentation.payments.PaymentsScreen
import com.konoec.polyworkapp.presentation.login.LoginScreen
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Login.route

    val showBottomBar = currentRoute != Screen.Login.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                PolyworkBottomBar(navController)
            }
        }
    ) { paddingValues ->

        AnimatedContent(
            targetState = currentRoute,
            label = "route_transition",
            transitionSpec = {
                val duration = 280
                val isEnteringHome = initialState == Screen.Login.route && targetState != Screen.Login.route

                if (isEnteringHome) {
                    (fadeIn(tween(duration)) + slideInHorizontally(tween(duration)) { it / 6 })
                        .togetherWith(fadeOut(tween(duration)) + slideOutHorizontally(tween(duration)) { -it / 6 })
                } else {
                    (fadeIn(tween(duration))).togetherWith(fadeOut(tween(duration)))
                }
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Login.route,
                modifier = Modifier.padding(paddingValues)
            ) {

                composable(Screen.Login.route) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(navController)
                }

                composable(Screen.Attendance.route) {
                    AttendanceScreen()
                }

                composable(Screen.Schedule.route) {
                    ScheduleScreen()
                }

                composable(Screen.Payments.route) {
                    PaymentsScreen()
                }
            }
        }
    }
}