package com.grupo4.clubdeportivo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.grupo4.clubdeportivo.database.dao.ActividadDAO
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

class NuevaActividadActivity : AppCompatActivity() {

    private var selectedImageUri: String? = null // Para guardar la ruta de la imagen

    // Definimos el lanzador de la galería
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val btnImagen = findViewById<ImageButton>(R.id.btnSeleccionarImagen)
            btnImagen.setImageURI(uri) // Muestra la imagen seleccionada en el botón
            btnImagen.setPadding(0, 0, 0, 0) // Quitamos el padding para que la foto llene el círculo
            selectedImageUri = uri.toString() // Guardamos la URI para la base de datos
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nueva_actividad)

        val etNombre = findViewById<TextInputEditText>(R.id.etNombreActividad)
        val etMonto = findViewById<TextInputEditText>(R.id.etMonto)
        val btnAgregar = findViewById<Button>(R.id.btnAgregar)
        val btnImagen = findViewById<ImageButton>(R.id.btnSeleccionarImagen)

        // Evento para abrir la galería de imagenes al tocar el círculo
        btnImagen.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnAgregar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val montoStr = etMonto.text.toString().trim()

            // Asignamos la URI guardada. Si es null, enviamos un texto vacío o una ruta por defecto.
            val urlImagen = selectedImageUri ?: ""

            if (nombre.isNotEmpty() && montoStr.isNotEmpty()) {
                val monto = montoStr.toDoubleOrNull() ?: 0.0 // Evita errores si el usuario pone caracteres raros

                val actividadDAO = ActividadDAO(this)

                // Enviamos la urlImagen al método
                val esValido = actividadDAO.insertarActividad(nombre, monto, urlImagen)

                if (esValido) {
                    Toast.makeText(this, "Actividad guardada: $nombre", Toast.LENGTH_SHORT).show()

                    // Limpiar campos
                    etNombre.text?.clear()
                    etMonto.text?.clear()
                    btnImagen.setImageResource(R.drawable.ic_actividad)
                    btnImagen.setPadding(30, 30, 30, 30) // Restauramos el padding original del icono
                    selectedImageUri = null

                    //Volvemos a la pantalla de Actividades
                    val aActividades = Intent(this, ActividadesActivity::class.java)
                    startActivity(aActividades)

                } else {
                    Toast.makeText(this, "Error al guardar en la base de datos", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
