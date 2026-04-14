package com.grupo4.clubdeportivo

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.grupo4.clubdeportivo.database.dao.ActividadDAO

class ActividadesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividades)

        val etNombre = findViewById<TextInputEditText>(R.id.etNombreActividad)
        val etMonto = findViewById<TextInputEditText>(R.id.etMonto)
        val btnAgregar = findViewById<Button>(R.id.btnAgregar)

        btnAgregar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val montoStr = etMonto.text.toString()

            if (nombre.isNotEmpty() && montoStr.isNotEmpty()) {
                val monto = montoStr.toDouble()

                // Aquí llamamos al ActividadDAO
                var actividadDAO = ActividadDAO(context = this)

                // Insertamos en la base de datos
                val esValido = actividadDAO.insertarActividad(nombre, monto)

                Toast.makeText(this, "Actividad guardada: $nombre", Toast.LENGTH_SHORT).show()

                // Limpiar campos
                etNombre.text?.clear()
                etMonto.text?.clear()
            } else {
                Toast.makeText(this, "Por favor completa los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

}