package com.grupo4.clubdeportivo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Usamos un Handler para ejecutar código después de un retraso
        Handler(Looper.getMainLooper()).postDelayed({
            // Creamos el Intent que nos envia al Login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            // Cerramos esta actividad para que el usuario no pueda volver atrás
            finish()
        }, 2000) // 2000 equivale a 2 segundos. PREGUNTAR A ANI SI QUIERE MENOS
    }
}