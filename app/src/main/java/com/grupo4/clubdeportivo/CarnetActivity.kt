package com.grupo4.clubdeportivo

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.grupo4.clubdeportivo.database.dao.SocioDAO

class CarnetActivity : AppCompatActivity() {

    private lateinit var socioDAO: SocioDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carnet)

        socioDAO = SocioDAO(this)

        // Capturar elementos de la UI
        val btnAtras = findViewById<ImageButton>(R.id.btnAtrasCarnet)
        val tvNombre = findViewById<TextView>(R.id.tvNombreCarnet)
        val tvDni = findViewById<TextView>(R.id.tvDniCarnet)
        val tvNumeroSocio = findViewById<TextView>(R.id.tvNumeroCarnetTarjeta)
        val btnCompartir = findViewById<MaterialButton>(R.id.btnCompartirCarnet)

        // Obtener el ID que nos mandó SocioActivity
        val socioId = intent.getIntExtra("EXTRA_SOCIO_ID", -1)

        if (socioId != -1) {
            // Buscamos el socio específico en la base de datos
            val socio = socioDAO.obtenerSocioPorId(socioId) // Asegúrate de tener este método en tu SocioDAO

            if (socio != null) {
                // Seteamos los textos idénticos a tu mockup
                tvNombre.text = "${socio.nombre} ${socio.apellido}"
                tvDni.text = "DNI: ${socio.nroDni}"

                // Formateamos los últimos números del carnet tal como hiciste en el Adapter
                val ultimosDigitos = socio.nroCarnet.takeLast(4).padStart(4, '0')
                tvNumeroSocio.text = "Socio N° $ultimosDigitos"
            }
        }

        // Acciones de los botones
        btnAtras.setOnClickListener {
            finish()
        }

        btnCompartir.setOnClickListener {
            // Aquí puedes meter la lógica para compartir los datos como texto o capturar la pantalla
        }
    }
}