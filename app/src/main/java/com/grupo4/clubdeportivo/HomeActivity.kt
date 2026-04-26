package com.grupo4.clubdeportivo

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val cvSocio = findViewById<MaterialCardView>(R.id.card_socios)
        val cvActividad = findViewById<MaterialCardView>(R.id.card_actividades)
        val cvPago = findViewById<MaterialCardView>(R.id.card_pagos)
        val cvListado = findViewById<MaterialCardView>(R.id.card_listado)

        cvActividad.setOnClickListener {
            val aActividades = Intent(this, ActividadesActivity::class.java)
            startActivity(aActividades)
        }

        cvPago.setOnClickListener {
            val aPagos = Intent(this, PagosActivity::class.java)
            startActivity(aPagos)
        }

        cvListado.setOnClickListener {
            val aListado = Intent(this, ListadoActivity::class.java)
            startActivity(aListado)
        }

        cvSocio.setOnClickListener {
            val aSocio = Intent(this, SocioActivity::class.java)
            startActivity(aSocio)
        }

    }
}