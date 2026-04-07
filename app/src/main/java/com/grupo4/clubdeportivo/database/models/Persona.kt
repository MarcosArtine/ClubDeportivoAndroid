package com.grupo4.clubdeportivo.database.models

open class Persona(
    open val nombre: String,
    open val apellido: String,
    open val tipoDni: String,
    open val nroDni: String,
    open val fechaNacimiento: String,
    open val mail: String,
    open val telefono: String
)