package com.grupo4.clubdeportivo.database.models

data class PagoRealizado(
    val pagoRealizadoId: Int = 0, // Autoincremental en DB
    val fechaPago: String? = null, // la base de datos debe poner el CURRENT_TIMESTAMP
    val montoTotal: Double,
    val tipoConcepto: String,
    val medioPago: String,
    val referenciaId: Int
)