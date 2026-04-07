package com.grupo4.clubdeportivo.database.models

data class NoSocio(
    val idNoSocio: Int = 0,

    // Atributos heredados de Persona
    override val nombre: String,
    override val apellido: String,
    override val tipoDni: String,
    override val nroDni: String,
    override val fechaNacimiento: String,
    override val mail: String,
    override val telefono: String
) : Persona(nombre, apellido, tipoDni, nroDni, fechaNacimiento, mail, telefono)