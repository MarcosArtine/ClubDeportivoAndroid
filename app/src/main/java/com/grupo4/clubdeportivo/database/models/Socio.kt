package com.grupo4.clubdeportivo.database.models

data class Socio(
    val idSocio: Int = 0,
    val fechaAltaSocio: String,
    val nroCarnet: String,
    val estadoSocio: String = "Activo",

    override val nombre: String,
    override val apellido: String,
    override val tipoDni: String,
    override val nroDni: String,
    override val fechaNacimiento: String,
    override val mail: String,
    override val telefono: String,
    override val aptoFisico: Boolean
) : Persona(nombre, apellido, tipoDni, nroDni, fechaNacimiento, mail, telefono, aptoFisico)
