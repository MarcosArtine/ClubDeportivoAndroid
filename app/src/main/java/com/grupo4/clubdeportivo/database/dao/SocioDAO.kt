package com.grupo4.clubdeportivo.database.dao

import android.content.ContentValues
import android.content.Context
import com.grupo4.clubdeportivo.database.BDatos
import com.grupo4.clubdeportivo.database.models.Socio

class SocioDAO(context: Context) {
    private val dbHelper = BDatos(context)

    fun insertarSocio(socio: Socio): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nombre", socio.nombre)
            put("apellido", socio.apellido)
            put("tipoDni", socio.tipoDni)
            put("nroDni", socio.nroDni)
            put("fechaNacimiento", socio.fechaNacimiento)
            put("mail", socio.mail)
            put("telefono", socio.telefono)
            put("fechaAlta", socio.fechaAltaSocio)
            put("nroCarnet", socio.nroCarnet)
        }

        val idGenerado = db.insert("Socios", null, values) // Asegúrate que el nombre de la tabla sea correcto
        db.close()
        return idGenerado
    }
    // Aqui iran las otras  funciones BorrarSocio, MostrarSocio, etc
}