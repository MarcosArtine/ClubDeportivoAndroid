package com.grupo4.clubdeportivo.database.models

data class Usuario(
    val usuarioId: Int = 0, //Al insertar un nuevo usuario, no se debe enviar el ID, para que el autoincremento haga su trabajo correctamente.
    val email: String,
    val contrasena: String,
    val nombre: String
)