package com.grupo4.clubdeportivo.database.dao

import android.content.ContentValues
import android.content.Context
import com.grupo4.clubdeportivo.database.BDatos

class SocioDAO(context: Context) {
    private val dbHelper = BDatos(context)

    fun insertarSocio(nombre: String, apellido: String, dni: Int): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues()

        values.put("nombre", nombre)
        values.put("apellido", apellido)
        values.put("dni", dni)

        return db.insert("Socio", null, values)
    }

    // Aqui iran las otras  funciones BorrarSocio, MostrarSocio, etc
}