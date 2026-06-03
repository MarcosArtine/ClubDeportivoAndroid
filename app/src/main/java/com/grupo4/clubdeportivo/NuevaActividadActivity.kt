package com.grupo4.clubdeportivo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.grupo4.clubdeportivo.database.dao.ActividadDAO
import com.grupo4.clubdeportivo.database.models.Actividad

class NuevaActividadActivity : AppCompatActivity() {

    private var selectedImageUri: String? = null
    private var esModoEditar = false
    private var actividadAEditar: Actividad? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val btnImagen = findViewById<ShapeableImageView>(R.id.btnSeleccionarImagen)
            btnImagen.setImageURI(uri)

            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, flag)

            selectedImageUri = uri.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nueva_actividad)

        // Referencias de la UI
        val tvTituloPantalla = findViewById<TextView>(R.id.tvNuevaActividad) // El TextView del título principal
        val etNombre = findViewById<TextInputEditText>(R.id.etNombreActividad)
        val etMonto = findViewById<TextInputEditText>(R.id.etMonto)
        val btnGuardar = findViewById<Button>(R.id.btnAgregar)
        val btnImagen = findViewById<ShapeableImageView>(R.id.btnSeleccionarImagen)

        // Evaluar si venimos en modo edición
        esModoEditar = intent.getBooleanExtra("MODO_EDITAR", false)

        if (esModoEditar) {
            // Recuperar el objeto serializable (compatible con versiones anteriores y nuevas de Android)
            actividadAEditar = @Suppress("DEPRECATION") intent.getSerializableExtra("ACTIVIDAD_OBJETO") as? Actividad

            // 1. Cambiar textos de la interfaz
            tvTituloPantalla.text = "EDITAR ACTIVIDAD"
            btnGuardar.text = "GUARDAR CAMBIOS"

            // 2. Precargar los campos con los datos existentes
            actividadAEditar?.let { actividad ->
                etNombre.setText(actividad.nombreActividad)
                etMonto.setText(actividad.montoActividad.toString())
                selectedImageUri = actividad.urlImagen

                if (!actividad.urlImagen.isNullOrEmpty()) {
                    try {
                        btnImagen.setImageURI(Uri.parse(actividad.urlImagen))
                    } catch (e: Exception) {
                        btnImagen.setImageResource(R.drawable.ic_actividad)
                    }
                }
            }
        }

        btnImagen.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val montoStr = etMonto.text.toString().trim()
            val urlImagen = selectedImageUri ?: ""

            if (nombre.isNotEmpty() && montoStr.isNotEmpty()) {
                val monto = montoStr.toDoubleOrNull() ?: 0.0
                val actividadDAO = ActividadDAO(this)

                val resultadoExitoso: Boolean

                if (esModoEditar) {
                    // Lógica de Actualización
                    // Pasamos el ID original que recuperamos del objeto
                    val id = actividadAEditar?.idActividad ?: 0

                    // TODO: Asegurate de que este método exista en tu ActividadDAO con esta estructura
                    resultadoExitoso = actividadDAO.actualizarActividad(id, nombre, monto, urlImagen)
                } else {
                    // Lógica de Inserción normal
                    resultadoExitoso = actividadDAO.insertarActividad(nombre, monto, urlImagen)
                }

                if (resultadoExitoso) {
                    val mensaje = if (esModoEditar) "Actividad actualizada" else "Actividad guardada: $nombre"
                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()

                    // Solo cerramos esta pantalla y la anterior reaccionará en su onResume()
                    finish()
                } else {
                    Toast.makeText(this, "Error al procesar en la base de datos", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }
}