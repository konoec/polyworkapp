package com.konoec.polyworkapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class StatsResponse(
    val body: StatsBody? = null,
    val header: ResponseHeader
)

@Serializable
data class StatsBody(
    val diasLaborados: Int = 0,
    val puntualidad: Int = 0
)

