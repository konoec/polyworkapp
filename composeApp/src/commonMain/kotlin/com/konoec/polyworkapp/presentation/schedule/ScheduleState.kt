package com.konoec.polyworkapp.presentation.schedule

data class ScheduleState(
    val isLoading: Boolean = false,
    val shifts: List<ScheduleItem> = emptyList(),
    val sessionExpired: Boolean = false
)

data class ScheduleItem(
    val day: String,
    val date: String,
    val time: String,
    val shiftType: String,
    val duration: String,
    val isConfirmed: Boolean
)

