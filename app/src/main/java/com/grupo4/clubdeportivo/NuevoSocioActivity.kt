package com.grupo4.clubdeportivo

import android.os.Bundle
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText

class NuevoSocioActivity : AppCompatActivity() {

    // EditTexts para capturar texto
    private lateinit var etNombre: TextInputEditText
    private lateinit var etApellido: TextInputEditText
    private lateinit var etNumDoc: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etFecha: TextInputEditText
    private lateinit var etCelular: TextInputEditText

    // Selectores (Dropdowns)
    private lateinit var actvTipoDoc: AutoCompleteTextView
    private lateinit var actvTipoCliente: AutoCompleteTextView

    // Switch
    private lateinit var switchApto: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_socio)

        initViews()
        setupDropdowns()
    }

    private fun initViews() {
        // En tu XML no se ve el ID de nombre/apellido, asumo estos nombres comunes:
        etNombre = findViewById(R.id.etNombre)
        etApellido = findViewById(R.id.etApellido)

        // Basado en tus capturas:
        etNumDoc = findViewById(R.id.etNumDoc)
        etEmail = findViewById(R.id.etEmail)
        etFecha = findViewById(R.id.etFecha)
        etCelular = findViewById(R.id.etCel)

        // AutoCompleteTextViews (dentro de los TextInputLayout)
        actvTipoDoc = findViewById(R.id.actvTipoDoc) // Asegúrate que este sea el ID del AutoCompleteTextView
        actvTipoCliente = findViewById(R.id.actvTipoCliente)

        // Switch
        switchApto = findViewById(R.id.switchApto)
    }

    private fun setupDropdowns() {
        // Ejemplo para Tipo de Documento
        val tiposDoc = arrayOf("DNI", "Pasaporte", "Cédula")
        val adapterDoc = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tiposDoc)
        actvTipoDoc.setAdapter(adapterDoc)

        // Ejemplo para Tipo de Cliente
        val tiposCliente = arrayOf("Socio", "No Socio")
        val adapterCliente = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tiposCliente)
        actvTipoCliente.setAdapter(adapterCliente)
    }

}

