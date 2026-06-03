package com.grupo4.clubdeportivo.database.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.grupo4.clubdeportivo.database.BDatos
import com.grupo4.clubdeportivo.database.models.Socio

class SocioDAO(context: Context) {

    private val dbHelper = BDatos(context)

    // INSERTAR — dos pasos dentro de una transacción: Persona y luego Socio
    fun insertarSocio(socio: Socio): Long {
        val db = dbHelper.writableDatabase
        var idGenerado = -1L

        db.beginTransaction()
        try {
            // Paso 1: insertar en Persona (atributos heredados)
            val valoresPersona = ContentValues().apply {
                put("Nombre", socio.nombre)
                put("Apellido", socio.apellido)
                put("TipoDni", socio.tipoDni)
                put("NroDni", socio.nroDni)
                put("FechaNacimiento", socio.fechaNacimiento)
                put("Telefono", socio.telefono)
                put("Email", socio.mail)
                // SQLite no tiene Boolean: 1 = true, 0 = false
                put("AptoFisico", if (socio.aptoFisico) 1 else 0)
            }
            val personaId = db.insert("Persona", null, valoresPersona)

            if (personaId == -1L) return -1L

            // insertamos en Socio usando el mismo ID que generó Persona
            val valoresSocio = ContentValues().apply {
                put("SocioId", personaId)
                put("FechaAltaSocio", socio.fechaAltaSocio)
                put("NroCarnet", socio.nroCarnet)
                put("EstadoSocio", socio.estadoSocio)
            }
            idGenerado = db.insert("Socio", null, valoresSocio)

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }

        return idGenerado
    }

    // OBTENER TODOS — JOIN entre Persona y Socio
    fun obtenerTodos(): List<Socio> {
        val lista = mutableListOf<Socio>()
        val db = dbHelper.readableDatabase

        val cursor: Cursor = db.rawQuery(
            """
            SELECT
                p.PersonaId, p.Nombre, p.Apellido, p.TipoDni, p.NroDni,
                p.FechaNacimiento, p.Email, p.Telefono, p.AptoFisico,
                s.FechaAltaSocio, s.NroCarnet, s.EstadoSocio
            FROM Socio s
            INNER JOIN Persona p ON s.SocioId = p.PersonaId
            ORDER BY p.Apellido, p.Nombre
            """.trimIndent(), null
        )

        while (cursor.moveToNext()) {
            lista.add(cursorASocio(cursor))
        }

        cursor.close()
        db.close()
        return lista
    }

    //Función para descargar el listado de socios morosos
    fun obtenerMorosos(): List<Socio> {
        val lista = mutableListOf<Socio>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT p.*, s.* FROM Socio s INNER JOIN Persona p ON s.SocioId = p.PersonaId WHERE s.EstadoSocio = 'Moroso'",
            null
        )

        // Lógica para llenar la lista recorriendo el cursor
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val socio = cursorASocio(cursor)
                lista.add(socio)
            }
            cursor.close()
        }
        db.close()
        return lista
    }

    // filtra por nombre o apellido para el buscador
    fun buscarPorNombreOApellido(texto: String): List<Socio> {
        val lista = mutableListOf<Socio>()
        val db = dbHelper.readableDatabase

        val cursor: Cursor = db.rawQuery(
            """
            SELECT
                p.PersonaId, p.Nombre, p.Apellido, p.TipoDni, p.NroDni,
                p.FechaNacimiento, p.Email, p.Telefono, p.AptoFisico,
                s.FechaAltaSocio, s.NroCarnet, s.EstadoSocio
            FROM Socio s
            INNER JOIN Persona p ON s.SocioId = p.PersonaId
            WHERE LOWER(p.Nombre) LIKE LOWER(?) OR LOWER(p.Apellido) LIKE LOWER(?)
            ORDER BY p.Apellido, p.Nombre
            """.trimIndent(),
            arrayOf("%$texto%", "%$texto%")
        )

        while (cursor.moveToNext()) {
            lista.add(cursorASocio(cursor))
        }

        cursor.close()
        db.close()
        return lista
    }

    // OBTENER POR ID — para la pantalla de detalle
    fun obtenerPorId(id: Int): Socio? {
        val db = dbHelper.readableDatabase

        val cursor: Cursor = db.rawQuery(
            """
            SELECT
                p.PersonaId, p.Nombre, p.Apellido, p.TipoDni, p.NroDni,
                p.FechaNacimiento, p.Email, p.Telefono, p.AptoFisico,
                s.FechaAltaSocio, s.NroCarnet, s.EstadoSocio
            FROM Socio s
            INNER JOIN Persona p ON s.SocioId = p.PersonaId
            WHERE s.SocioId = ?
            """.trimIndent(),
            arrayOf(id.toString())
        )

        val socio = if (cursor.moveToFirst()) cursorASocio(cursor) else null

        cursor.close()
        db.close()
        return socio
    }

    // ACTUALIZAR — modifica Persona y Socio con transacción
    fun actualizarSocio(socio: Socio): Boolean {
        val db = dbHelper.writableDatabase
        var exito = false

        db.beginTransaction()
        try {
            val valoresPersona = ContentValues().apply {
                put("Nombre", socio.nombre)
                put("Apellido", socio.apellido)
                put("TipoDni", socio.tipoDni)
                put("NroDni", socio.nroDni)
                put("FechaNacimiento", socio.fechaNacimiento)
                put("Telefono", socio.telefono)
                put("Email", socio.mail)
                put("AptoFisico", if (socio.aptoFisico) 1 else 0)
            }
            db.update("Persona", valoresPersona, "PersonaId = ?", arrayOf(socio.idSocio.toString()))

            val valoresSocio = ContentValues().apply {
                put("EstadoSocio", socio.estadoSocio)
                put("NroCarnet", socio.nroCarnet)
            }
            db.update("Socio", valoresSocio, "SocioId = ?", arrayOf(socio.idSocio.toString()))

            db.setTransactionSuccessful()
            exito = true
        } finally {
            db.endTransaction()
            db.close()
        }

        return exito
    }

    // DAR DE BAJA — baja lógica, cambia estado a "Inactivo"
    fun darDeBaja(id: Int): Boolean {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("EstadoSocio", "Inactivo")
        }
        val filasAfectadas = db.update("Socio", valores, "SocioId = ?", arrayOf(id.toString()))
        db.close()
        return filasAfectadas > 0
    }

    // Convierte una fila del cursor en un objeto Socio
    private fun cursorASocio(cursor: Cursor): Socio {
        return Socio(
            idSocio         = cursor.getInt(cursor.getColumnIndexOrThrow("PersonaId")),
            nombre          = cursor.getString(cursor.getColumnIndexOrThrow("Nombre")),
            apellido        = cursor.getString(cursor.getColumnIndexOrThrow("Apellido")),
            tipoDni         = cursor.getString(cursor.getColumnIndexOrThrow("TipoDni")) ?: "",
            nroDni          = cursor.getString(cursor.getColumnIndexOrThrow("NroDni")),
            fechaNacimiento = cursor.getString(cursor.getColumnIndexOrThrow("FechaNacimiento")) ?: "",
            mail            = cursor.getString(cursor.getColumnIndexOrThrow("Email")) ?: "",
            telefono        = cursor.getString(cursor.getColumnIndexOrThrow("Telefono")) ?: "",
            aptoFisico      = cursor.getInt(cursor.getColumnIndexOrThrow("AptoFisico")) == 1,
            fechaAltaSocio  = cursor.getString(cursor.getColumnIndexOrThrow("FechaAltaSocio")),
            nroCarnet       = cursor.getString(cursor.getColumnIndexOrThrow("NroCarnet")),
            estadoSocio     = cursor.getString(cursor.getColumnIndexOrThrow("EstadoSocio")) ?: "Activo"
        )
    }
}