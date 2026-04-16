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

        val cvActivity = findViewById<MaterialCardView>(R.id.card_actividades)
        val cvPagos = findViewById<MaterialCardView>(R.id.card_pagos)

        cvActivity.setOnClickListener {
            val aActividades = Intent(this, ActividadesActivity::class.java)
            startActivity(aActividades)
        }

        cvPagos.setOnClickListener {
            val aPagos = Intent(this, PagosActivity::class.java)
            startActivity(aPagos)
        }

    }
}