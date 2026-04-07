package com.grupo4.clubdeportivo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialización de las vistas
        val etUsuario = findViewById<TextInputEditText>(R.id.etUsuario)
        val etContrasena = findViewById<TextInputEditText>(R.id.etContrasena)
        val tvOlvideContrasena = findViewById<TextView>(R.id.tvOlvideContrasena)
        val btnIniciarSesion = findViewById<Button>(R.id.btnIniciarSesion)
        val tvRegistrateAqui = findViewById<TextView>(R.id.tvRegistrateAqui)

        // Configuración de los clicks

        btnIniciarSesion.setOnClickListener {
            val usuario = etUsuario.text.toString()
            val contrasena = etContrasena.text.toString()

            if (usuario.isNotEmpty() && contrasena.isNotEmpty()) {
                // Lógica de autenticación
                if (usuario == "marcos" && contrasena == "12345678") {
                    Toast.makeText(this, "¡Ingreso Exitoso!", Toast.LENGTH_SHORT).show()
                    Log.d("LoginActivity", "Login exitoso para usuario: $usuario")
                    // Te redirige a la siguiente pantalla (Intent)
                    val aHome = Intent(this, HomeActivity::class.java)
                    startActivity(aHome)
                } else {
                    Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Aquí inicia la activity de recuperación de contraseña
        tvOlvideContrasena.setOnClickListener {
            //Toast.makeText Proporciona una pequeña ventana emergente con información
            Toast.makeText(this, "Ir a pantalla 'Olvidé contraseña'", Toast.LENGTH_SHORT).show()
        }

        // Aquí inicia la activity de registro
        tvRegistrateAqui.setOnClickListener {
            // Te redirige a la siguiente pantalla (Intent)
            val aRegister = Intent(this, RegisterActivity::class.java)
            startActivity(aRegister)
            Toast.makeText(this, "Ir a pantalla 'Registro'", Toast.LENGTH_SHORT).show()
        }
    }
}