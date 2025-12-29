package com.konoec.polyworkapp.presentation.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Extrae solo la hora de un string en formato ISO 8601 o formato simple de hora
 * Ejemplos:
 * - "2025-12-27T08:00" -> "08:00"
 * - "08:00" -> "08:00"
 */
fun extractTimeFromDateTime(dateTimeString: String): String {
    return if (dateTimeString.contains("T")) {
        dateTimeString.substringAfter("T")
    } else {
        dateTimeString
    }
}

/**
 * Obtiene la fecha de un string en formato ISO 8601
 * Ejemplo: "2025-12-29T08:00" -> LocalDate(2025, 12, 29)
 */
fun parseDateFromDateTime(dateTimeString: String): LocalDate? {
    return try {
        if (dateTimeString.contains("T")) {
            val parts = dateTimeString.split("T")[0].split("-")
            if (parts.size == 3) {
                LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            } else null
        } else null
    } catch (_: Exception) {
        null
    }
}

/**
 * Calcula la descripción relativa de una fecha
 * - Si es hoy: "Hoy {hora}"
 * - Si es mañana: "Mañana {hora}"
 * - Si es en 2 días: "Pasado mañana {hora}"
 * - Otro día: "{día} de {mes} a las {hora}"
 */
fun formatNextShiftTime(nextShiftTime: String?): String {
    if (nextShiftTime.isNullOrEmpty()) {
        return "Disfruta tu descanso"
    }

    val time = extractTimeFromDateTime(nextShiftTime)
    val date = parseDateFromDateTime(nextShiftTime) ?: return "Próximo ingreso: $time"

    val now = kotlin.time.Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val daysDiff = date.toEpochDays() - today.toEpochDays()

    return when {
        daysDiff == 0L -> "Hoy a las $time"
        daysDiff == 1L -> "Mañana a las $time"
        daysDiff == 2L -> "Pasado mañana a las $time"
        daysDiff in 3..6 -> {
            val dayName = when (date.dayOfWeek) {
                kotlinx.datetime.DayOfWeek.MONDAY -> "Lunes"
                kotlinx.datetime.DayOfWeek.TUESDAY -> "Martes"
                kotlinx.datetime.DayOfWeek.WEDNESDAY -> "Miércoles"
                kotlinx.datetime.DayOfWeek.THURSDAY -> "Jueves"
                kotlinx.datetime.DayOfWeek.FRIDAY -> "Viernes"
                kotlinx.datetime.DayOfWeek.SATURDAY -> "Sábado"
                kotlinx.datetime.DayOfWeek.SUNDAY -> "Domingo"
            }
            "$dayName a las $time"
        }
        else -> {
            val monthName = when (date.month) {
                kotlinx.datetime.Month.JANUARY -> "enero"
                kotlinx.datetime.Month.FEBRUARY -> "febrero"
                kotlinx.datetime.Month.MARCH -> "marzo"
                kotlinx.datetime.Month.APRIL -> "abril"
                kotlinx.datetime.Month.MAY -> "mayo"
                kotlinx.datetime.Month.JUNE -> "junio"
                kotlinx.datetime.Month.JULY -> "julio"
                kotlinx.datetime.Month.AUGUST -> "agosto"
                kotlinx.datetime.Month.SEPTEMBER -> "septiembre"
                kotlinx.datetime.Month.OCTOBER -> "octubre"
                kotlinx.datetime.Month.NOVEMBER -> "noviembre"
                kotlinx.datetime.Month.DECEMBER -> "diciembre"
            }
            "${date.dayOfMonth} de $monthName a las $time"
        }
    }
}

/**
 * Formatea el horario de trabajo actual
 * Extrae las horas de inicio y fin si vienen en formato ISO 8601
 */
fun formatWorkSchedule(startTime: String, endTime: String): String {
    val start = extractTimeFromDateTime(startTime)
    val end = extractTimeFromDateTime(endTime)
    return "Horario: $start - $end"
}

/**
 * Calcula la duración de un turno desde el formato "HH:MM - HH:MM"
 * Ejemplo: "08:00 - 17:30" -> "9.5 hrs"
 */
fun calculateShiftDuration(timeRange: String): String {
    return try {
        val parts = timeRange.split("-").map { it.trim() }
        if (parts.size != 2) return "-"

        val (startHour, startMin) = parts[0].split(":").map { it.toInt() }
        val (endHour, endMin) = parts[1].split(":").map { it.toInt() }

        val startMinutes = startHour * 60 + startMin
        val endMinutes = endHour * 60 + endMin
        val durationMinutes = endMinutes - startMinutes

        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60

        return if (minutes == 0) {
            "$hours hrs"
        } else {
            val decimalHours = hours + (minutes / 60.0)
            String.format("%.1f hrs", decimalHours)
        }
    } catch (_: Exception) {
        "-"
    }
}

/**
 * Verifica si una fecha en formato "dd/MM/yyyy" es hoy
 * Ejemplo: "27/12/2025" -> true (si hoy es 27 de diciembre de 2025)
 */
fun isToday(dateString: String): Boolean {
    return try {
        val parts = dateString.split("/")
        if (parts.size != 3) return false

        val day = parts[0].toInt()
        val month = parts[1].toInt()
        val year = parts[2].toInt()

        val date = LocalDate(year, month, day)
        val now = kotlin.time.Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

        date == today
    } catch (_: Exception) {
        false
    }
}

/**
 * Extrae el día de una fecha en formato "dd/MM/yyyy"
 * Ejemplo: "27/12/2025" -> "27"
 */
fun extractDayFromDate(dateString: String): String {
    return try {
        dateString.split("/").firstOrNull() ?: dateString
    } catch (_: Exception) {
        dateString
    }
}


