package com.konoec.polyworkapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Stats(
    val diasLaborados: Int,
    val puntualidad: Int  // Porcentaje 0-100
)

