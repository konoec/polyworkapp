package com.konoec.polyworkapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MotivoJustificacionResponse(
    val body: List<MotivoJustificacion>,
    val header: ResponseHeader
)

@Serializable
data class MotivoJustificacion(
    val id: Int,
    val descripcion: String
)

