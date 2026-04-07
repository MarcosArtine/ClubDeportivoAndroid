package com.grupo4.clubdeportivo.database.models

data class Usuario(
    val usuarioId: Int = 0,
    val userName: String,
    val password: String,
    val rolId: Int,
    val activo: Boolean,
)