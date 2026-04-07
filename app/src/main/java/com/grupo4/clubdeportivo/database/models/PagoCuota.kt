package com.grupo4.clubdeportivo.database.models

data class PagoCuota(
    val cuotaId: Int = 0,
    val socioId: Int, // Relación con Socio
    val fechaVencimientoCuota: String,
    val estadoCuota: String,
    val montoCuota: Double,
    val medioPago: String,
    val mesCuota: Int,
    val anioCuota: Int
)