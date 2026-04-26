package com.grupo4.clubdeportivo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.grupo4.clubdeportivo.database.dao.UsuarioDAO

class LoginActivity : AppCompatActivity() {


    private val PREFS_NAME = "LoginPrefs"
    private val KEY_REMEMBER = "recordar"
    private val KEY_EMAIL = "email"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicialización de las vistas
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etContrasena = findViewById<TextInputEditText>(R.id.etContrasena)
        val tvOlvideContrasena = findViewById<TextView>(R.id.tvOlvideContrasena)
        val btnIniciarSesion = findViewById<Button>(R.id.btnIniciarSesion)
        val tvRegistrateAqui = findViewById<TextView>(R.id.tvRegistrateAqui)
        val cbRecuerdame = findViewById<CheckBox>(R.id.cbRecuerdame)


        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val recordar = sharedPreferences.getBoolean(KEY_REMEMBER, false)

        if (recordar) {
            val emailGuardado = sharedPreferences.getString(KEY_EMAIL, "")
            etEmail.setText(emailGuardado)
            cbRecuerdame.isChecked = true

            etContrasena.requestFocus() //Si el email ya está puesto ponemos el cursor en la contraseña
        }

        btnIniciarSesion.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            if (email.isNotEmpty() && contrasena.isNotEmpty()) {

                //Hashamos la contraseña por seguridad
                val contrasenaHasheada = SeguridadUtils.hashPassword(contrasena)

                //Instanciamos UsuarioDAO
                val usuarioDao = UsuarioDAO(this)

                // Consultamos si existe el usuario a la base de datos
                val esValido = usuarioDao.buscarUsuario(email, contrasenaHasheada)

                if (esValido){
                    Toast.makeText(this, "¡Ingreso Exitoso!", Toast.LENGTH_SHORT).show()

                    // Usamos la variable que declaramos en el onCreate
                    val editor = sharedPreferences.edit()

                    if (cbRecuerdame.isChecked) {
                        editor.putBoolean(KEY_REMEMBER, true)
                        editor.putString(KEY_EMAIL, email)
                    } else {
                        // Si no está marcado, limpiamos los datos guardados
                        editor.remove(KEY_REMEMBER)
                        editor.remove(KEY_EMAIL)
                    }
                    editor.apply()

                    Log.d("LoginActivity", "Login exitoso")
                    // Te redirige a la siguiente pantalla Home
                    val aHome = Intent(this, HomeActivity::class.java)
                    startActivity(aHome)
                    finish()  //Finaliza LoginActivity para que no vuelvan atrás con el botón físico
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
            // Te redirige a la siguiente pantalla de registro
            val aRegister = Intent(this, RegisterActivity::class.java)
            startActivity(aRegister)
            Toast.makeText(this, "Ir a pantalla 'Registro'", Toast.LENGTH_SHORT).show()
        }
    }

}