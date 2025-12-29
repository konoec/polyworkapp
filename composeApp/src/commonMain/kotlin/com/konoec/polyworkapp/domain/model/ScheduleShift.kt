package com.konoec.polyworkapp.domain.model

data class ScheduleShift(
    val day: String,
    val date: String,
    val time: String,
    val shiftType: String,
    val confirmed: Boolean
)

