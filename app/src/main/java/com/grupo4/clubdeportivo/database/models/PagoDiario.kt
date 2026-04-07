package com.grupo4.clubdeportivo.database.models

data class PagoDiario(
    val pagoDiarioId: Int = 0,
    val noSocioId: Int, // Relación con NoSocio
    val actividadId: Int, // Relación con Actividad
    val fechaPago: String,
    val montoPagado: Double // Aquí no va coma
)