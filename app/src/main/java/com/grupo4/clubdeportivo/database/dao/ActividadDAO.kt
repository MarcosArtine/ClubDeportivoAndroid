package com.grupo4.clubdeportivo.database.dao

import android.content.ContentValues
import android.content.Context
import com.grupo4.clubdeportivo.database.BDatos
import com.grupo4.clubdeportivo.database.models.Actividad

class ActividadDAO(context: Context) {
    private val dbHelper = BDatos(context)

    fun insertarActividad(nuevaActividad: String, montoActividad: Double, nuevaUrl: String): Boolean {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("NombreActividad", nuevaActividad)
            put("MontoActividad", montoActividad)
            put("URLImagen", nuevaUrl)
        }
        val resultado = db.insert("Actividad", null, valores)
        db.close()
        return resultado != -1L
    }

    fun borrarActividad(idABorrar: Int): Int {
        val db = dbHelper.writableDatabase
        // Se usa ActividadId de manera consistente
        val filaBorrada = db.delete("Actividad", "ActividadId = ?", arrayOf(idABorrar.toString()))
        db.close()
        return filaBorrada
    }

    fun buscarActividad(nombreABuscar: String): Boolean {
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM Actividad WHERE NombreActividad = ?"
        val cursor = db.rawQuery(query, arrayOf(nombreABuscar))

        val existe = cursor.moveToFirst()
        cursor.close()
        db.close()
        return existe
    }

    fun actualizarActividad(id: Int, nombre: String, monto: Double, urlImagen: String): Boolean {
        val db = dbHelper.writableDatabase

        val cv = ContentValues().apply {
            put("NombreActividad", nombre)
            put("MontoActividad", monto)
            put("URLImagen", urlImagen)
        }

        val resultado = db.update(
            "Actividad",
            cv,
            "ActividadId = ?",
            arrayOf(id.toString())
        )

        db.close()
        return resultado > 0
    }

    fun listarActividades(): List<Actividad> {
        val lista = mutableListOf<Actividad>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Actividad", null)

        if (cursor.moveToFirst()) {
            // Buscamos los índices de forma dinámica para evitar errores si cambia el orden en la DB
            val idIdx = cursor.getColumnIndex("ActividadId")
            val nombreIdx = cursor.getColumnIndex("NombreActividad")
            val montoIdx = cursor.getColumnIndex("MontoActividad")
            val urlIdx = cursor.getColumnIndex("URLImagen")

            do {
                val actividad = Actividad(
                    idActividad = if (idIdx != -1) cursor.getInt(idIdx) else 0,
                    nombreActividad = if (nombreIdx != -1) cursor.getString(nombreIdx) else "",
                    montoActividad = if (montoIdx != -1) cursor.getDouble(montoIdx) else 0.0,
                    urlImagen = if (urlIdx != -1) cursor.getString(urlIdx) else ""
                )
                lista.add(actividad)
            } while (cursor.moveToNext())
        }

        // RESPUESTA A TU DUDA (????): Sí, es excelente práctica cerrar el cursor y la base de datos aquí
        cursor.close()
        db.close()
        return lista
    }
}