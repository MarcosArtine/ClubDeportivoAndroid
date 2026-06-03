package com.grupo4.clubdeportivo.database.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.grupo4.clubdeportivo.database.BDatos
import com.grupo4.clubdeportivo.database.models.NoSocio

class NoSocioDAO(context: Context) {

    private val dbHelper = BDatos(context)

    // INSERTAR — Dos pasos dentro de una transacción: Persona y luego NoSocio
    fun insertarNoSocio(noSocio: NoSocio): Long {
        val db = dbHelper.writableDatabase
        var idGenerado = -1L

        db.beginTransaction()
        try {
            // Paso 1: Insertar en Persona (atributos heredados)
            val valoresPersona = ContentValues().apply {
                put("Nombre", noSocio.nombre)
                put("Apellido", noSocio.apellido)
                put("TipoDni", noSocio.tipoDni)
                put("NroDni", noSocio.nroDni)
                put("FechaNacimiento", noSocio.fechaNacimiento)
                put("Telefono", noSocio.telefono)
                put("Email", noSocio.mail)
                put("AptoFisico", if (noSocio.aptoFisico) 1 else 0)
            }
            val personaId = db.insert("Persona", null, valoresPersona)

            if (personaId == -1L) return -1L

            // Paso 2: Insertar en NoSocio usando el mismo ID que generó Persona
            val valoresNoSocio = ContentValues().apply {
                put("NoSocioId", personaId)
            }
            idGenerado = db.insert("NoSocio", null, valoresNoSocio)

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }

        return idGenerado
    }

    // OBTENER TODOS — JOIN entre Persona y NoSocio
    fun obtenerTodos(): List<NoSocio> {
        val lista = mutableListOf<NoSocio>()
        val db = dbHelper.readableDatabase

        val cursor: Cursor = db.rawQuery(
            """
            SELECT 
                p.PersonaId, p.Nombre, p.Apellido, p.TipoDni, p.NroDni, 
                p.FechaNacimiento, p.Email, p.Telefono, p.AptoFisico
            FROM NoSocio ns
            INNER JOIN Persona p ON ns.NoSocioId = p.PersonaId
            ORDER BY p.Apellido, p.Nombre
            """.trimIndent(), null
        )

        while (cursor.moveToNext()) {
            lista.add(cursorANoSocio(cursor))
        }

        cursor.close()
        db.close()
        return lista
    }

    // Convierte una fila del cursor en un objeto NoSocio
    private fun cursorANoSocio(cursor: Cursor): NoSocio {
        return NoSocio(
            idNoSocio       = cursor.getInt(cursor.getColumnIndexOrThrow("PersonaId")),
            nombre          = cursor.getString(cursor.getColumnIndexOrThrow("Nombre")),
            apellido        = cursor.getString(cursor.getColumnIndexOrThrow("Apellido")),
            tipoDni         = cursor.getString(cursor.getColumnIndexOrThrow("TipoDni")) ?: "",
            nroDni          = cursor.getString(cursor.getColumnIndexOrThrow("NroDni")),
            fechaNacimiento = cursor.getString(cursor.getColumnIndexOrThrow("FechaNacimiento")) ?: "",
            mail            = cursor.getString(cursor.getColumnIndexOrThrow("Email")) ?: "",
            telefono        = cursor.getString(cursor.getColumnIndexOrThrow("Telefono")) ?: "",
            aptoFisico      = cursor.getInt(cursor.getColumnIndexOrThrow("AptoFisico")) == 1
        )
    }
}