package com.grupo4.clubdeportivo

import java.security.MessageDigest

object SeguridadUtils {

    //Función de hasheo para la contraseña a lo largo del proyecto
    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}