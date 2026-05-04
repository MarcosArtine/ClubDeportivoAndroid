package com.grupo4.clubdeportivo

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.grupo4.clubdeportivo.database.dao.SocioDAO
import com.grupo4.clubdeportivo.database.models.Socio
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetalleSocioActivity : AppCompatActivity() {

    // Referencias a las vistas del layout
    private lateinit var etNombre: TextInputEditText
    private lateinit var etApellido: TextInputEditText
    private lateinit var etNumDoc: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etFecha: TextInputEditText
    private lateinit var etCelular: TextInputEditText
    private lateinit var actvTipoDoc: AutoCompleteTextView
    private lateinit var actvEstado: AutoCompleteTextView
    private lateinit var switchApto: SwitchMaterial
    private lateinit var btnGuardar: MaterialButton
    private lateinit var btnEliminar: MaterialButton
    private lateinit var btnVolver: ImageButton

    // DAO y datos del socio
    private lateinit var socioDAO: SocioDAO
    private var socioActual: Socio? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_socio)

        socioDAO = SocioDAO(this)

        inicializarVistas()
        setupDropdowns()

        val socioId = intent.getIntExtra(EXTRA_SOCIO_ID, -1)

        if (socioId == -1) {
            Toast.makeText(this, "Error: no se encontró el socio", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Cargamos el socio desde la BD y precargamos los campos
        cargarSocio(socioId)

        etFecha.setOnClickListener {
            mostrarCalendario()
        }

        btnGuardar.setOnClickListener {
            guardarCambios()
        }

        btnEliminar.setOnClickListener {
            confirmarBaja()
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun cargarSocio(id: Int) {
        socioActual = socioDAO.obtenerPorId(id)

        if (socioActual == null) {
            Toast.makeText(this, "No se encontró el socio", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val socio = socioActual!!
        etNombre.setText(socio.nombre)
        etApellido.setText(socio.apellido)
        actvTipoDoc.setText(socio.tipoDni, false)  // false = no filtrar el dropdown
        etNumDoc.setText(socio.nroDni)
        etEmail.setText(socio.mail)
        etFecha.setText(socio.fechaNacimiento)
        etCelular.setText(socio.telefono)
        actvEstado.setText(socio.estadoSocio, false)
        switchApto.isChecked = socio.aptoFisico
    }

    private fun guardarCambios() {
        val nombre = etNombre.text.toString().trim()
        val apellido = etApellido.text.toString().trim()
        val tipoDoc = actvTipoDoc.text.toString()
        val numDoc = etNumDoc.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val fecha = etFecha.text.toString().trim()
        val celular = etCelular.text.toString().trim()
        val estado = actvEstado.text.toString()
        val tieneApto = switchApto.isChecked

        if (nombre.isEmpty() || apellido.isEmpty() || numDoc.isEmpty()) {
            Toast.makeText(this, "Nombre, apellido y documento son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val socioActualizado = socioActual!!.copy(
            nombre = nombre,
            apellido = apellido,
            tipoDni = tipoDoc,
            nroDni = numDoc,
            mail = email,
            fechaNacimiento = fecha,
            telefono = celular,
            estadoSocio = estado,
            aptoFisico = tieneApto
        )

        val exito = socioDAO.actualizarSocio(socioActualizado)
        if (exito) {
            Toast.makeText(this, "Cambios guardados correctamente", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)  // Le avisamos a SocioActivity que hubo cambios
            finish()
        } else {
            Toast.makeText(this, "Error al guardar los cambios", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDropdowns() {
        val tiposDoc = arrayOf("DNI", "Pasaporte", "Cédula")
        val adapterDoc = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tiposDoc)
        actvTipoDoc.setAdapter(adapterDoc)

        val estados = arrayOf("Activo", "Inactivo", "Moroso")
        val adapterEstado = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, estados)
        actvEstado.setAdapter(adapterEstado)
    }

    private fun mostrarCalendario() {
        val calendar = Calendar.getInstance()
        val fechaActual = etFecha.text.toString()

        if (fechaActual.isNotEmpty()) {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                calendar.time = sdf.parse(fechaActual)!!
            } catch (e: Exception) {

            }
        }

        val anio = calendar.get(Calendar.YEAR)
        val mes = calendar.get(Calendar.MONTH)
        val dia = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, year, month, day ->
            val mesActualizado = month + 1
            val diaFormateado = if (day < 10) "0$day" else "$day"
            val mesFormateado = if (mesActualizado < 10) "0$mesActualizado" else "$mesActualizado"
            etFecha.setText("$diaFormateado/$mesFormateado/$year")
        }, anio, mes, dia).also { dialogo ->
            dialogo.datePicker.maxDate = System.currentTimeMillis()
            dialogo.show()
        }
    }

    private fun confirmarBaja() {
        val socio = socioActual ?: return
        AlertDialog.Builder(this)
            .setTitle("Dar de baja")
            .setMessage("¿Confirmas la baja de ${socio.nombre} ${socio.apellido}?\n\nEl socio quedará como Inactivo.")
            .setPositiveButton("Dar de baja") { _, _ ->
                val exito = socioDAO.darDeBaja(socio.idSocio)
                if (exito) {
                    Toast.makeText(this, "Socio dado de baja correctamente", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this, "Error al dar de baja", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun inicializarVistas() {
        etNombre = findViewById(R.id.etNombreDetalle)
        etApellido = findViewById(R.id.etApellidoDetalle)
        etNumDoc = findViewById(R.id.etNumDocDetalle)
        etEmail = findViewById(R.id.etEmailDetalle)
        etFecha = findViewById(R.id.etFechaDetalle)
        etCelular = findViewById(R.id.etCelDetalle)
        actvTipoDoc = findViewById(R.id.actvTipoDocDetalle)
        actvEstado = findViewById(R.id.actvEstadoDetalle)
        switchApto = findViewById(R.id.switchAptoDetalle)
        btnGuardar = findViewById(R.id.btnGuardarDetalle)
        btnEliminar = findViewById(R.id.btnDarDeBaja)
        btnVolver = findViewById(R.id.btnRetrocederDetalle)
    }

    companion object {
        const val EXTRA_SOCIO_ID = "socio_id"
    }
}