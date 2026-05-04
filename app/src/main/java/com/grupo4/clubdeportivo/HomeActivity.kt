package com.grupo4.clubdeportivo

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
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
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)

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

        btnMenu.setOnClickListener {
            mostrarMenuLateral()
        }


    }

    private fun mostrarMenuLateral() {
        // Aplicamos el estilo personalizado que creamos en el paso 1
        val dialog = Dialog(this, R.style.MenuLateralDialog)
        val view = layoutInflater.inflate(R.layout.dialog_menu, null)
        dialog.setContentView(view)

        // Configuramos la posición en la pantalla
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            params.gravity = android.view.Gravity.START // Pega el diálogo a la izquierda
            params.width = (resources.displayMetrics.widthPixels * 0.7).toInt() // 70% del ancho de pantalla
            params.height = android.view.WindowManager.LayoutParams.MATCH_PARENT // Todo el alto
            window.attributes = params
        }

        // Acción para el botón de cerrar sesión
        view.findViewById<LinearLayout>(R.id.btnCerrarSesion).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            dialog.dismiss()
        }

        dialog.show()
    }


}