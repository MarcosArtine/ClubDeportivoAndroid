package com.grupo4.clubdeportivo

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SocioActivity : AppCompatActivity() {

   private lateinit var btnNuevoSocio : FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_socio)

        btnNuevoSocio = findViewById(R.id.btnNuevoSocio)


        btnNuevoSocio.setOnClickListener {
            val aNuevoSocio = Intent(this, NuevoSocioActivity::class.java)
            startActivity(aNuevoSocio)
        }

    }
}