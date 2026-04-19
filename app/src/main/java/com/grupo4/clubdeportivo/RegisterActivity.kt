package com.grupo4.clubdeportivo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.grupo4.clubdeportivo.database.dao.UsuarioDAO

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Inicialización de las vistas
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etContrasena = findViewById<TextInputEditText>(R.id.etContrasena)
        val etNombre = findViewById<TextInputEditText>(R.id.etNombre)
        val btnCrearCuenta = findViewById<Button>(R.id.btnCrearCuenta)

        btnCrearCuenta.setOnClickListener {
            val email = etEmail.text.toString()
            val contrasena = etContrasena.text.toString()
            val nombre = etNombre.text.toString()

            if (email.isNotEmpty() && contrasena.isNotEmpty() && nombre.isNotEmpty()) {

                // Validación de seguridad de la contraseña usan la función esContrasenaValida
                if (!esContrasenaValida(contrasena)) {
                    Toast.makeText(
                        this,
                        "La contraseña debe tener al menos 8 caracteres, un número, una mayúscula y una minúscula",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener // Detiene la ejecución aquí
                }

                //Hashamos la contraseña por seguridad
                val contrasenaHasheada = SeguridadUtils.hashPassword(contrasena)

                //Instanciamos UsuarioDAO
                val usuarioDao = UsuarioDAO(this)

                // Consultamos si existe el usuario a la base de datos
                val esValido = usuarioDao.insertarUsuario(email, contrasenaHasheada, nombre)

                // Si el usuario fue registrado obtendra un Id que sera mayo o igual a 0
                if (esValido >= 0) {
                    Toast.makeText(this, "¡Registro Exitoso!", Toast.LENGTH_SHORT).show()
                    Log.d("RegisterActivity", "Registro exitoso")
                    // Te redirige a la siguiente pantalla Home
                    val aLogin = Intent(this, MainActivity::class.java)
                    startActivity(aLogin)
                } else {
                    Toast.makeText(this, "No se pudo registrar", Toast.LENGTH_SHORT).show()

                }

            }

        }

    }

    //Función de validación de la contraseña
    private fun esContrasenaValida(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$"
        return password.matches(passwordPattern.toRegex())
    }
}
