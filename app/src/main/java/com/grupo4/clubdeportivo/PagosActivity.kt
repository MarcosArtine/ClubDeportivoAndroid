package com.grupo4.clubdeportivo

import android.content.ContentValues
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.grupo4.clubdeportivo.database.BDatos
import com.grupo4.clubdeportivo.database.dao.ActividadDAO
import com.grupo4.clubdeportivo.database.dao.PagoCuotaDAO
import com.grupo4.clubdeportivo.database.dao.PagoDiarioDAO
import com.grupo4.clubdeportivo.database.dao.SocioDAO
import java.text.SimpleDateFormat
import java.util.*

// PagosActivity — pantalla de pagos.
// Diseño segun el prototipo en Figma:
//   - Lista principal de pagos realizados (Pagos / Pagos4)
//   - Botón "+" → abre formulario Nuevo Pago (Pagos1)
//   - Formulario: ID → autocompleta Tipo cliente, Nombre, Apellido
//   - Tipo de pago: Cuota o Actividad
//   - Medio de pago: Efectivo / Tarjeta / Tarjeta (3 pagos) / Tarjeta (6 pagos)
//   - Botón PAGAR → registra y vuelve a la lista
class PagosActivity : AppCompatActivity() {

    // ---- Vistas pantalla principal (lista) ----
    private lateinit var recyclerPagos: RecyclerView
    private lateinit var tvSinPagos: TextView
    private lateinit var btnNuevoPago: View           // botón "+" rojo del Figma

    // ---- Vistas formulario Nuevo Pago ----
    private lateinit var layoutLista: View            // pantalla lista
    private lateinit var layoutFormulario: View       // pantalla formulario

    private lateinit var etId: TextInputEditText                // campo ID
    private lateinit var btnBuscarId: View                      // al salir del campo busca
    private lateinit var spinnerTipoCliente: Spinner            // Socio / No Socio
    private lateinit var etNombre: TextInputEditText
    private lateinit var etApellido: TextInputEditText
    private lateinit var spinnerTipoPago: Spinner               // Cuota / Actividad
    private lateinit var spinnerActividad: Spinner              // visible solo si Actividad
    private lateinit var layoutActividad: View
    private lateinit var spinnerMedioPago: Spinner              // Efectivo / Tarjeta / etc.
    private lateinit var etImporte: TextInputEditText
    private lateinit var btnPagar: Button
    private lateinit var btnVolver: View                        // flecha atrás del Figma

    // ---- DAOs ----
    private lateinit var pagoCuotaDAO: PagoCuotaDAO
    private lateinit var pagoDiarioDAO: PagoDiarioDAO
    private lateinit var actividadDAO: ActividadDAO

    // ---- Estado ----
    private var idEncontrado: Int = -1
    private var tipoClienteEncontrado: String = ""  // "Socio" o "No Socio"
    private var actividadIdSeleccionada: Int = -1
    private var montoActividad: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagos)

        pagoCuotaDAO = PagoCuotaDAO(this)
        pagoDiarioDAO = PagoDiarioDAO(this)
        actividadDAO  = ActividadDAO(this)

        inicializarVistas()
        configurarSpinners()
        configurarEventos()
        cargarListaPagos()
    }

    private fun inicializarVistas() {
        layoutLista       = findViewById(R.id.layoutListaPagos)
        layoutFormulario  = findViewById(R.id.layoutFormulario)
        recyclerPagos     = findViewById(R.id.recyclerPagos)
        tvSinPagos        = findViewById(R.id.tvSinPagos)
        btnNuevoPago      = findViewById(R.id.btnNuevoPago)

        etId              = findViewById(R.id.etId)
        spinnerTipoCliente = findViewById(R.id.spinnerTipoCliente)
        etNombre          = findViewById(R.id.etNombre)
        etApellido        = findViewById(R.id.etApellido)
        spinnerTipoPago   = findViewById(R.id.spinnerTipoPago)
        layoutActividad   = findViewById(R.id.layoutActividad)
        spinnerActividad  = findViewById(R.id.spinnerActividad)
        spinnerMedioPago  = findViewById(R.id.spinnerMedioPago)
        etImporte         = findViewById(R.id.etImporte)
        btnPagar          = findViewById(R.id.btnPagar)
        btnVolver         = findViewById(R.id.btnVolver)

        // Al iniciar mostramos la lista, el formulario está oculto
        layoutLista.visibility      = View.VISIBLE
        layoutFormulario.visibility = View.GONE
    }

    private fun configurarSpinners() {

        // Spinner: Tipo de cliente (Socio / No Socio) — solo lectura, se llena al buscar ID
        spinnerTipoCliente.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item,
            listOf("Socio", "No Socio")
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Spinner: Tipo de pago
        spinnerTipoPago.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item,
            listOf("Cuota", "Actividad")
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Cuando cambia el tipo de pago, mostramos/ocultamos el spinner de actividades
        spinnerTipoPago.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                layoutActividad.visibility = if (pos == 1) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        // Spinner: Actividades (desde la base de datos)
        cargarSpinnerActividades()

        // Spinner: Medio de pago — opciones del Figma (Frame 9)
        spinnerMedioPago.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item,
            listOf("Efectivo", "Tarjeta", "Tarjeta (3 pagos)", "Tarjeta (6 pagos)")
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Al seleccionar actividad, actualizamos el monto sugerido
        spinnerActividad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                val actividades = obtenerListaActividades()
                if (actividades.isNotEmpty() && pos < actividades.size) {
                    actividadIdSeleccionada = actividades[pos].first
                    montoActividad = actividades[pos].second
                    etImporte.setText(montoActividad.toString())
                }
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }

    // Carga el spinner de actividades leyendo desde la base de datos
    private fun cargarSpinnerActividades() {
        val actividades = obtenerListaActividades()
        val nombres = if (actividades.isEmpty()) listOf("Sin actividades") else actividades.map { it.third }
        spinnerActividad.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, nombres
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    // Retorna lista de (id, monto, nombre) de actividades
    private fun obtenerListaActividades(): List<Triple<Int, Double, String>> {
        val db = BDatos(this).readableDatabase
        val lista = mutableListOf<Triple<Int, Double, String>>()
        val cursor = db.rawQuery("SELECT ActividadId, MontoActividad, NombreActividad FROM Actividad", null)
        while (cursor.moveToNext()) {
            lista.add(Triple(
                cursor.getInt(0),
                cursor.getDouble(1),
                cursor.getString(2)
            ))
        }
        cursor.close()
        db.close()
        return lista
    }

    private fun configurarEventos() {

        // Botón "+" → mostrar formulario Nuevo Pago
        btnNuevoPago.setOnClickListener {
            limpiarFormulario()
            layoutLista.visibility      = View.GONE
            layoutFormulario.visibility = View.VISIBLE
        }

        // Botón volver (flecha atrás del Figma) → volver a la lista
        btnVolver.setOnClickListener {
            layoutFormulario.visibility = View.GONE
            layoutLista.visibility      = View.VISIBLE
            cargarListaPagos()
        }

        // Campo ID: al perder foco busca el cliente
        // (equivale al comportamiento del Figma: escribís "1" → aparece "Lucas / Medina / Socio")
        etId.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) buscarClientePorId()
        }

        // Botón PAGAR
        btnPagar.setOnClickListener {
            registrarPago()
        }
    }

    // Busca socio o no socio por ID y autocompleta el formulario — igual al Figma Pagos2/Pagos3
    private fun buscarClientePorId() {

        val idStr = etId.text.toString().trim()
        val id = idStr.toIntOrNull() ?: return

        val db = BDatos(this).readableDatabase

        // Buscamos primero en Socio
        var cursor = db.rawQuery(
            "SELECT p.Nombre, p.Apellido FROM Persona p INNER JOIN Socio s ON p.PersonaId = s.SocioId WHERE s.SocioId = ?",
            arrayOf(id.toString())
        )

        if (cursor.moveToFirst()) {
            etNombre.setText(cursor.getString(0))
            etApellido.setText(cursor.getString(1))
            spinnerTipoCliente.setSelection(0) // "Socio"
            idEncontrado = id
            tipoClienteEncontrado = "Socio"
            cursor.close()
            db.close()
            return
        }
        cursor.close()

        // Si no está en Socio, buscamos en NoSocio
        cursor = db.rawQuery(
            "SELECT p.Nombre, p.Apellido FROM Persona p INNER JOIN NoSocio ns ON p.PersonaId = ns.NoSocioId WHERE ns.NoSocioId = ?",
            arrayOf(id.toString())
        )

        if (cursor.moveToFirst()) {
            etNombre.setText(cursor.getString(0))
            etApellido.setText(cursor.getString(1))
            spinnerTipoCliente.setSelection(1) // "No Socio"
            idEncontrado = id
            tipoClienteEncontrado = "No Socio"
        } else {
            // Escenario 4: ID no encontrado
            Toast.makeText(this, "El ID $id no corresponde a ningún registro", Toast.LENGTH_SHORT).show()
            etNombre.setText("")
            etApellido.setText("")
            idEncontrado = -1
            tipoClienteEncontrado = ""
        }

        cursor.close()
        db.close()
    }

    // Registra el pago según el tipo seleccionado
    private fun registrarPago() {

        if (idEncontrado == -1) {
            Toast.makeText(this, "Ingresá un ID válido primero", Toast.LENGTH_SHORT).show()
            return
        }

        val importe = etImporte.text.toString().trim().toDoubleOrNull()
        if (importe == null || importe <= 0) {
            Toast.makeText(this, "Ingresá un importe válido", Toast.LENGTH_SHORT).show()
            return
        }

        val medioPago  = spinnerMedioPago.selectedItem.toString()
        val tipoPago   = spinnerTipoPago.selectedItem.toString()

        when {
            tipoPago == "Cuota" && tipoClienteEncontrado == "Socio" -> {
                registrarPagoCuota(importe, medioPago)
            }
            tipoPago == "Actividad" && tipoClienteEncontrado == "No Socio" -> {
                registrarPagoDiario(importe, medioPago)
            }
            tipoPago == "Cuota" && tipoClienteEncontrado == "No Socio" -> {
                Toast.makeText(this, "Los No Socios no pagan cuota. Seleccioná Actividad.", Toast.LENGTH_SHORT).show()
            }
            tipoPago == "Actividad" && tipoClienteEncontrado == "Socio" -> {
                Toast.makeText(this, "Para pago de actividad el cliente debe ser No Socio.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registrarPagoCuota(importe: Double, medioPago: String) {

        val cal = Calendar.getInstance()
        val mes  = cal.get(Calendar.MONTH) + 1
        val anio = cal.get(Calendar.YEAR)

        // Verificar pago duplicado del mes
        if (pagoCuotaDAO.yaPagoElMes(idEncontrado, mes, anio)) {
            AlertDialog.Builder(this)
                .setTitle("Pago duplicado")
                .setMessage("Este socio ya pagó la cuota de $mes/$anio. ¿Registrar de todas formas?")
                .setPositiveButton("Sí") { _, _ -> guardarCuota(importe, medioPago, mes, anio) }
                .setNegativeButton("No", null)
                .show()
            return
        }
        guardarCuota(importe, medioPago, mes, anio)
    }

    private fun guardarCuota(importe: Double, medioPago: String, mes: Int, anio: Int) {

        // Determinar si es plan de cuotas según el medio de pago
        val cantCuotas = when (medioPago) {
            "Tarjeta (3 pagos)" -> 3
            "Tarjeta (6 pagos)" -> 6
            else -> 1
        }

        if (cantCuotas == 1) {
            // Pago único
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance().apply { set(anio, mes - 1, 1) }
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            val fechaVenc = sdf.format(cal.time)

            val resultado = pagoCuotaDAO.insertarPagoCuota(
                socioId         = idEncontrado,
                fechaVencimiento = fechaVenc,
                montoCuota      = importe,
                medioPago       = medioPago,
                mesCuota        = mes,
                anioCuota       = anio
            )

            if (resultado != -1L) {
                mostrarComprobante("Cuota mensual", importe, medioPago, "Comprobante N° $resultado")
            } else {
                Toast.makeText(this, "Error al registrar el pago", Toast.LENGTH_SHORT).show()
            }

        } else {
            // Plan de cuotas
            val cuotas = pagoCuotaDAO.insertarPlanCuotas(
                socioId        = idEncontrado,
                montoTotal     = importe,
                cantidadCuotas = cantCuotas,
                medioPago      = medioPago,
                mesInicio      = mes,
                anioInicio     = anio
            )

            if (cuotas.isNotEmpty()) {
                val detalle = cuotas.mapIndexed { i, c ->
                    "Cuota ${i + 1}: ${c.mesCuota}/${c.anioCuota} — $${"%.2f".format(c.montoCuota)} — ${c.estadoCuota}"
                }.joinToString("\n")
                mostrarComprobantePlan(cantCuotas, importe, medioPago, detalle)
            } else {
                Toast.makeText(this, "Error al registrar el plan de cuotas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registrarPagoDiario(importe: Double, medioPago: String) {

        if (actividadIdSeleccionada == -1) {
            Toast.makeText(this, "Seleccioná una actividad", Toast.LENGTH_SHORT).show()
            return
        }

        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val resultado = pagoDiarioDAO.insertarPagoDiario(
            noSocioId   = idEncontrado,
            actividadId = actividadIdSeleccionada,
            montoPagado = importe,
            medioPago   = medioPago,
            fechaPago   = hoy
        )

        if (resultado != -1L) {
            mostrarComprobante("Actividad", importe, medioPago, "Comprobante N° $resultado")
        } else {
            Toast.makeText(this, "Error al registrar el pago", Toast.LENGTH_SHORT).show()
        }
    }

    // Muestra el comprobante — el Figma menciona "comprobante disponible para compartir"
    private fun mostrarComprobante(tipo: String, monto: Double, medio: String, comprobante: String) {
        AlertDialog.Builder(this)
            .setTitle("Pago registrado")
            .setMessage("Tipo: $tipo\nMonto: $${"%.2f".format(monto)}\nMedio: $medio\n\n$comprobante")
            .setPositiveButton("OK") { _, _ ->
                layoutFormulario.visibility = View.GONE
                layoutLista.visibility      = View.VISIBLE
                cargarListaPagos()
            }
            .setNeutralButton("Compartir") { _, _ ->
                Toast.makeText(this, "Función compartir en desarrollo", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun mostrarComprobantePlan(cantCuotas: Int, monto: Double, medio: String, detalle: String) {
        AlertDialog.Builder(this)
            .setTitle("Plan $cantCuotas cuotas registrado")
            .setMessage("Total: $${"%.2f".format(monto)}\nMedio: $medio\n\n$detalle")
            .setPositiveButton("OK") { _, _ ->
                layoutFormulario.visibility = View.GONE
                layoutLista.visibility      = View.VISIBLE
                cargarListaPagos()
            }
            .setNeutralButton("Compartir") { _, _ ->
                Toast.makeText(this, "Función compartir en desarrollo", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    // Carga la lista unificada de pagos (Cuotas + Diarios) para la pantalla principal
    private fun cargarListaPagos() {

        val pagos = mutableListOf<Map<String, String>>()
        pagos.addAll(pagoCuotaDAO.listarPagosConDetalle())
        pagos.addAll(pagoDiarioDAO.listarPagosDiariosConDetalle())

        // Ordenamos por fecha descendente
        val ordenados = pagos.sortedByDescending { it["fecha"] }

        if (ordenados.isEmpty()) {
            recyclerPagos.visibility = View.GONE
            tvSinPagos.visibility    = View.VISIBLE
        } else {
            tvSinPagos.visibility    = View.GONE
            recyclerPagos.visibility = View.VISIBLE
            recyclerPagos.layoutManager = LinearLayoutManager(this)
            recyclerPagos.adapter = PagoListaAdapter(ordenados)
        }
    }

    private fun limpiarFormulario() {
        etId.text?.clear()
        etNombre.setText("")
        etApellido.setText("")
        etImporte.text?.clear()
        spinnerTipoPago.setSelection(0)
        spinnerMedioPago.setSelection(0)
        layoutActividad.visibility = View.GONE
        idEncontrado = -1
        tipoClienteEncontrado = ""
    }
}

// =============================================================================
// ADAPTER de la lista de pagos — diseño del Figma (Pagos / Pagos4)
// Muestra: fecha | nombre apellido | $monto | ícono comprobante
// El nombre se muestra en rojo como en el Figma
// =============================================================================
class PagoListaAdapter(
    private val pagos: List<Map<String, String>>
) : RecyclerView.Adapter<PagoListaAdapter.PagoViewHolder>() {

    inner class PagoViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvFecha: TextView    = v.findViewById(R.id.tvFechaPago)
        val tvNombre: TextView   = v.findViewById(R.id.tvNombrePago)
        val tvMonto: TextView    = v.findViewById(R.id.tvMontoPago)
        val tvTipo: TextView     = v.findViewById(R.id.tvTipoPago)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PagoViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_pago, parent, false)
        )

    override fun onBindViewHolder(h: PagoViewHolder, pos: Int) {
        val p = pagos[pos]
        h.tvFecha.text  = p["fecha"] ?: ""
        h.tvNombre.text = "${p["nombre"]} ${p["apellido"]}"
        h.tvMonto.text  = "$${p["monto"]}"
        h.tvTipo.text   = if (p["tipo"] == "Cuota") "Cuota" else (p["actividad"] ?: "Actividad")
    }

    override fun getItemCount() = pagos.size
}
