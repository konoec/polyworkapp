package com.konoec.polyworkapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PayslipListResponse(
    val header: ResponseHeader,
    val body: PayslipListBody? = null
)

@Serializable
data class PayslipListBody(
    val boletas: List<PayslipDto>
)

@Serializable
data class PayslipDto(
    val id: Int,
    val numero: String,
    val periodo: Int,
    val mes: Int,
    val periodoLabel: String,
    val trabajador: String,
    val dni: String,
    val fechaGeneracion: String,
    val validado: String,
    val fechaValidacion: String? = null
)

@Serializable
data class ValidatePayslipRequest(
    val boletaId: Int,
    val deviceId: String? = null,
    val latitud: String? = null,
    val longitud: String? = null
)

@Serializable
data class ValidatePayslipResponse(
    val header: ResponseHeader,
    val body: ValidatePayslipBody? = null
)

@Serializable
data class ValidatePayslipBody(
    val success: Boolean,
    val message: String,
    val numero: String,
    val fechaValidacion: String
)
