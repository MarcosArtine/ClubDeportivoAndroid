package com.grupo4.clubdeportivo.database.models

data class Actividad(
    val idActividad: Int = 0,
    val nombreActividad: String,
    val montoActividad: Double // Usamos Double para importes de dinero
)