package com.grupo4.clubdeportivo.database.models

data class Socio(
    val idSocio: Int = 0, //El 0 indica que aún no tiene ID en la base de datos.
    val fechaAltaSocio: String,
    val nroCarnet: String,

    // Atributos heredados de Persona
    override val nombre: String,
    override val apellido: String,
    override val tipoDni: String,
    override val nroDni: String,
    override val fechaNacimiento: String,
    override val mail: String,
    override val telefono: String
) : Persona(nombre, apellido, tipoDni, nroDni, fechaNacimiento, mail, telefono)