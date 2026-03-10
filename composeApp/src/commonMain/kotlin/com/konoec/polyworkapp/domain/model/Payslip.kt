package com.konoec.polyworkapp.domain.model

data class Payslip(
    val id: Int,
    val numero: String,
    val periodo: Int,
    val mes: Int,
    val periodoLabel: String,
    val trabajador: String,
    val dni: String,
    val fechaGeneracion: String,
    val validado: Boolean,
    val fechaValidacion: String?
)
