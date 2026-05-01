package com.grupo4.clubdeportivo

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import android.app.DatePickerDialog
import android.widget.Button
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.grupo4.clubdeportivo.database.dao.PagoCuotaDAO
import com.grupo4.clubdeportivo.database.dao.SocioDAO
import com.grupo4.clubdeportivo.database.models.Socio
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NuevoSocioActivity : AppCompatActivity() {

    private lateinit var etNombre: TextInputEditText
    private lateinit var etApellido: TextInputEditText
    private lateinit var etNumDoc: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etFecha: TextInputEditText
    private lateinit var etCelular: TextInputEditText
    private lateinit var actvTipoDoc: AutoCompleteTextView
    private lateinit var actvTipoCliente: AutoCompleteTextView
    private lateinit var switchApto: SwitchMaterial
    private lateinit var btnRegistrar: MaterialButton

    private lateinit var socioDAO: SocioDAO


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_socio)

        socioDAO = SocioDAO(this)

        inicializarVistas()
        setupDropdowns()

        // Abrimos el selector para elegir la fecha de nacimiento
        etFecha.setOnClickListener {
            mostrarCalendario()
        }

        btnRegistrar.setOnClickListener {
            // 1. Capturar datos de la UI
            val nombre = etNombre.text.toString().trim()
            val apellido = etApellido.text.toString().trim()
            val tipoDoc = actvTipoDoc.text.toString()
            val numDoc = etNumDoc.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val fechaNac = etFecha.text.toString().trim()
            val celular = etCelular.text.toString().trim()
            val tipoCliente = actvTipoCliente.text.toString()
            val tieneApto = switchApto.isChecked


            if (nombre.isEmpty() || apellido.isEmpty() || numDoc.isEmpty() || tipoCliente.isEmpty()) {
                Toast.makeText(this, "Por favor, completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Creamos el Socio segun el modelo (FALTA EL APTO FISICO AVISAR A ANI!!!)
            // El idSocio es 0 por defecto
            val nuevoSocio = Socio(
                idSocio = 0,
                fechaAltaSocio = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                nroCarnet = "SOC-${System.currentTimeMillis()}", // Generación simple de carnet
                nombre = nombre,
                apellido = apellido,
                tipoDni = tipoDoc,
                nroDni = numDoc,
                fechaNacimiento = fechaNac,
                mail = email,
                telefono = celular
            )

            val resultado = socioDAO.insertarSocio(nuevoSocio)

            if (resultado > 0) {
                Toast.makeText(this, "Socio registrado con éxito!", Toast.LENGTH_LONG).show()
                finish() // Cerramos la actividad y volvemos atrás
            } else {
                Toast.makeText(this, "Error al registrar el socio", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun inicializarVistas() {
        etNombre = findViewById(R.id.etNombre)
        etApellido = findViewById(R.id.etApellido)
        etNumDoc = findViewById(R.id.etNumDoc)
        etEmail = findViewById(R.id.etEmail)
        etFecha = findViewById(R.id.etFecha)
        etCelular = findViewById(R.id.etCel)
        actvTipoDoc = findViewById(R.id.actvTipoDoc)
        actvTipoCliente = findViewById(R.id.actvTipoCliente)
        switchApto = findViewById(R.id.switchApto)
        btnRegistrar = findViewById(R.id.btnRegistrar)
    }

    //Función para opciones de los desplegables
    private fun setupDropdowns() {
        val tiposDoc = arrayOf("DNI", "Pasaporte", "Cédula")
        val adapterDoc = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tiposDoc)
        actvTipoDoc.setAdapter(adapterDoc)

        val tiposCliente = arrayOf("Socio", "No Socio")
        val adapterCliente = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tiposCliente)
        actvTipoCliente.setAdapter(adapterCliente)
    }

    private fun mostrarCalendario() {
        // Obtenemos la fecha actual para que el calendario abra en el día de hoy
        val calendario = Calendar.getInstance()
        val anio = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        // Creamos el diálogo
        val dialogo = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            // monthOfYear va de 0 a 11, por eso sumamos 1
            val mesActualizado = monthOfYear + 1

            // Formateamos la fecha para que siempre tenga dos dígitos (ej: 05/09/2024)
            val diaFormateado = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
            val mesFormateado = if (mesActualizado < 10) "0$mesActualizado" else "$mesActualizado"

            val fechaSeleccionada = "$diaFormateado/$mesFormateado/$year"

            // Seteamos el texto en el EditText
            etFecha.setText(fechaSeleccionada)

        }, anio, mes, dia)

        /*Usamos esta linea de codigo para que el usuario no pueda elegir fechas futuras
        ya que la función la usamos para elegir la fecha de nacimiento solamente */
        dialogo.datePicker.maxDate = System.currentTimeMillis()

        dialogo.show()
    }


}

