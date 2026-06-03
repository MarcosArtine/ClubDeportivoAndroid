package com.grupo4.clubdeportivo.database.models

import java.io.Serializable

// La hacemos Serializable para poder pasarlo a otro activity
data class Actividad(
    val idActividad: Int = 0,
    val nombreActividad: String,
    val montoActividad: Double, // Usamos Double para importes de dinero
    val urlImagen: String? = null
) : Serializable