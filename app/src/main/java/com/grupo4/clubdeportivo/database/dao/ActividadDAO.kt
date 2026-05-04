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
       // Si es distinto de -1 devuelve true, si es -1 devuelve false.
        return resultado != -1L
    }

    fun borrarActividad(idABorrar: Int): Int {
        val db = dbHelper.writableDatabase
        val filaBorrada = db.delete("Actividad", "ActividadId = ?", arrayOf(idABorrar.toString()))
        db.close()
        return filaBorrada
    }


    fun buscarActividad(nombreABuscar: String): Boolean {
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM Actividad WHERE NombreActividad = ?"
        val cursor = db.rawQuery(query, arrayOf(nombreABuscar))

        val existe = cursor.moveToFirst() // Si hay al menos un registro me va a devolver true
        cursor.close()
        db.close()
        return existe
    }

    fun actualizarActividad(idAActualizar: Int, nuevoNombre: String, nuevoMonto: Double): Int {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("NombreActividad", nuevoNombre)
            put("MontoActividad", nuevoMonto)
        }
        val filaActualizada =
            db.update("Actividad", valores, "ActividadId = ?", arrayOf(idAActualizar.toString()))
        db.close()
        return filaActualizada
    }

    fun listarActividades(): List<Actividad> {
        val lista = mutableListOf<Actividad>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Actividad", null)

        if (cursor.moveToFirst()) {
            do {
                val actividad = Actividad(
                    cursor.getInt(0), // ActividadId
                    cursor.getString(1), // NombreActividad
                    cursor.getDouble(2), // MontoActividad
                    cursor.getString(3) // URLIamgen
                )
                lista.add(actividad)
            } while (cursor.moveToNext())
        }
        //???? db.close()
        cursor.close()
        return lista
    }

}