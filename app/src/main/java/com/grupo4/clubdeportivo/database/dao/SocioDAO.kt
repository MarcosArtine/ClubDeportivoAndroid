package com.grupo4.clubdeportivo.database.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.grupo4.clubdeportivo.database.BDatos
import com.grupo4.clubdeportivo.database.models.Socio

class SocioDAO(context: Context) {

    private val dbHelper = BDatos(context)

    fun insertarSocio(socio: Socio): Long {
        val db = dbHelper.writableDatabase
        var idGenerado = -1L

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
                put("AptoFisico", if (socio.aptoFisico) 1 else 0) // guarda 1 (true) o 0 (false)
            }
            val personaId = db.insert("Persona", null, valoresPersona)

            // Si personaId es -1, insert falló (ej: NroDni duplicado)
            if (personaId == -1L) return -1L

            val valoresSocio = ContentValues().apply {
                put("SocioId", personaId)          // FK → Persona.PersonaId
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
            """.trimIndent(),
            null
        )

        while (cursor.moveToNext()) {
            lista.add(cursorASocio(cursor))
        }

        cursor.close()
        db.close()
        return lista
    }

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
            arrayOf("%$texto%", "%$texto%")   // El % es comodín, como * en búsquedas
        )

        while (cursor.moveToNext()) {
            lista.add(cursorASocio(cursor))
        }

        cursor.close()
        db.close()
        return lista
    }

    fun obtenerPorId(id: Int): Socio? {
        val db = dbHelper.readableDatabase

        val cursor: Cursor = db.rawQuery(
            """
            SELECT
                p.PersonaId, p.Nombre, p.Apellido, p.TipoDni, p.NroDni,
                p.FechaNacimiento, p.Email, p.Telefono, p.AptoFisico,
                s.FechaAltaSocio, s.NroCarnet, s.Estad oSocio
            FROM Socio s
            INNER JOIN Persona p ON s.SocioId = p.PersonaId
            WHERE s.SocioId = ?
            """.trimIndent(),
            arrayOf(id.toString())
        )

        // Si hay resultado, mapeamos; si no, devolvemos null
        val socio = if (cursor.moveToFirst()) cursorASocio(cursor) else null

        cursor.close()
        db.close()
        return socio
    }

    fun actualizarSocio(socio: Socio): Boolean {
        val db = dbHelper.writableDatabase
        var exito = false

        db.beginTransaction()
        try {
            // Actualizamos la tabla Persona
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

            // Actualizamos la tabla Socio
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

    fun darDeBaja(id: Int): Boolean {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("EstadoSocio", "Inactivo")
        }
        val filasAfectadas = db.update("Socio", valores, "SocioId = ?", arrayOf(id.toString()))
        db.close()
        // update() devuelve el número de filas modificadas; > 0 significa éxito
        return filasAfectadas > 0
    }

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
            // getInt devuelve 1 o 0; comparamos con 1 para obtener Boolean
            aptoFisico      = cursor.getInt(cursor.getColumnIndexOrThrow("AptoFisico")) == 1,
            fechaAltaSocio  = cursor.getString(cursor.getColumnIndexOrThrow("FechaAltaSocio")),
            nroCarnet       = cursor.getString(cursor.getColumnIndexOrThrow("NroCarnet")),
            estadoSocio     = cursor.getString(cursor.getColumnIndexOrThrow("EstadoSocio")) ?: "Activo"
        )
    }
}
