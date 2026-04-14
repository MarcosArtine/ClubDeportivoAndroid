
package com.grupo4.clubdeportivo.database.dao

import android.content.ContentValues
import android.content.Context
import com.grupo4.clubdeportivo.database.BDatos

class UsuarioDAO(context: Context) {
    private val dbHelper = BDatos(context)

    fun insertarUsuario(nuevoEmail:String, nuevaContrasena: String, nuevoNombre: String): Long {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("Emain", nuevoEmail)
            put("Contrasena", nuevaContrasena)
            put("Nombre", nuevoNombre)
        }
        val id = db.insert("Usuario", null, valores)
        db.close()
        return id
    }

    fun borrarUsuario(idABorrar: Int): Int {
        val db = dbHelper.writableDatabase
        // Es más seguro usar el método delete que execSQL para evitar inyecciones SQL
        val filasBorradas = db.delete("Usuario", "UsuarioId = ?", arrayOf(idABorrar.toString()))
        db.close()
        return filasBorradas
    }

    fun buscarUsuario(email: String, contrasena: String): Boolean {
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM Usuario WHERE Email = ? AND Contrasena = ?"
        val cursor = db.rawQuery(query, arrayOf(email, contrasena))

        val existe = cursor.moveToFirst() // Si hay al menos un registro me va a devolver true
        cursor.close()
        db.close()
        return existe
    }

    fun actualizarNombreUsuario(idAActualizar: Int, nuevoNombre: String): Int {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("Nombre", nuevoNombre)
        }
        val filaActualizada = db.update("Usuario", valores, "UsuarioId = ?", arrayOf(idAActualizar.toString()))
        db.close()
        return filaActualizada
    }

    fun actualizarEmailUsuario(idAActualizar: Int, nuevoEmail: String): Int {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("Email", nuevoEmail)
        }
        val filaActualizada = db.update("Usuario", valores, "UsuarioId = ?", arrayOf(idAActualizar.toString()))
        db.close()
        return filaActualizada
    }

    fun actualizarContrasenaUsuario(idAActualizar: Int, nuevaContrasena: String): Int {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("Contrasena", nuevaContrasena)
        }
        val filaActualizada = db.update("Usuario", valores, "UsuarioId = ?", arrayOf(idAActualizar.toString()))
        db.close()
        return filaActualizada
    }

}