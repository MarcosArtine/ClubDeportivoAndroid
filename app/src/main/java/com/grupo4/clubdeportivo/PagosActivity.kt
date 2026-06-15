package com.grupo4.clubdeportivo

import android.content.ContentValues
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.grupo4.clubdeportivo.database.BDatos
import com.grupo4.clubdeportivo.database.dao.ActividadDAO
import com.grupo4.clubdeportivo.database.dao.PagoCuotaDAO
import com.grupo4.clubdeportivo.database.dao.PagoDiarioDAO
import com.grupo4.clubdeportivo.database.dao.SocioDAO
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class PagosActivity : AppCompatActivity() {

    private lateinit var recyclerPagos: RecyclerView
    private lateinit var tvSinPagos: TextView
    private lateinit var btnNuevoPago: View

    private lateinit var layoutLista: View
    private lateinit var layoutFormulario: View

    private lateinit var etId: TextInputEditText

    private lateinit var btnBuscarCliente: com.google.android.material.button.MaterialButton
    private lateinit var tvClienteEncontrado: TextView
    private lateinit var spinnerTipoCliente: Spinner
    private lateinit var etNombre: TextInputEditText
    private lateinit var etApellido: TextInputEditText
    private lateinit var spinnerTipoPago: Spinner
    private lateinit var spinnerActividad: Spinner
    private lateinit var layoutActividad: View
    private lateinit var spinnerMedioPago: Spinner
    private lateinit var etImporte: TextInputEditText
    private lateinit var btnPagar: Button
    private lateinit var btnVolver: View

    private lateinit var btnVolverPrincipal: ImageButton

    // ---- DAOs ----
    private lateinit var pagoCuotaDAO: PagoCuotaDAO
    private lateinit var pagoDiarioDAO: PagoDiarioDAO
    private lateinit var actividadDAO: ActividadDAO

    // ---- Estado ----
    private var idEncontrado: Int = -1
    private var tipoClienteEncontrado: String = ""
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
        layoutLista = findViewById(R.id.layoutListaPagos)
        layoutFormulario = findViewById(R.id.layoutFormulario)
        recyclerPagos           = findViewById(R.id.recyclerPagos)
        tvSinPagos              = findViewById(R.id.tvSinPagos)
        btnNuevoPago            = findViewById(R.id.btnNuevoPago)

        etId                    = findViewById(R.id.etId)
        btnBuscarCliente    = findViewById(R.id.btnBuscarCliente)
        tvClienteEncontrado = findViewById(R.id.tvClienteEncontrado)
        tvClienteEncontrado.visibility = View.GONE
        spinnerTipoCliente      = findViewById(R.id.spinnerTipoCliente)
        etNombre                = findViewById(R.id.etNombre)
        etApellido              = findViewById(R.id.etApellido)
        spinnerTipoPago         = findViewById(R.id.spinnerTipoPago)
        layoutActividad         = findViewById(R.id.layoutActividad)
        spinnerActividad        = findViewById(R.id.spinnerActividad)
        spinnerMedioPago        = findViewById(R.id.spinnerMedioPago)
        etImporte               = findViewById(R.id.etImporte)
        btnPagar                = findViewById(R.id.btnPagar)
        btnVolver               = findViewById(R.id.btnVolver)
        btnVolverPrincipal      = findViewById(R.id.btnVolverPrincipal)

        layoutLista.visibility      = View.VISIBLE
        layoutFormulario.visibility = View.GONE
    }

    private fun configurarSpinners() {

        spinnerTipoCliente.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item,
            listOf("Socio", "No Socio")
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinnerTipoPago.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item,
            listOf("Cuota", "Actividad")
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinnerTipoPago.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                layoutActividad.visibility = if (pos == 1) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        cargarSpinnerActividades()

        spinnerMedioPago.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item,
            listOf("Efectivo", "Tarjeta", "Tarjeta (3 pagos)", "Tarjeta (6 pagos)")
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

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

    private fun cargarSpinnerActividades() {
        val actividades = obtenerListaActividades()
        val nombres = if (actividades.isEmpty()) listOf("Sin actividades") else actividades.map { it.third }
        spinnerActividad.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, nombres
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun obtenerListaActividades(): List<Triple<Int, Double, String>> {
        val db = BDatos(this).readableDatabase
        val lista = mutableListOf<Triple<Int, Double, String>>()
        val cursor = db.rawQuery("SELECT ActividadId, MontoActividad, NombreActividad FROM Actividad", null)
        while (cursor.moveToNext()) {
            lista.add(Triple(cursor.getInt(0), cursor.getDouble(1), cursor.getString(2)))
        }
        cursor.close()
        db.close()
        return lista
    }

    private fun configurarEventos() {

        btnNuevoPago.setOnClickListener {
            limpiarFormulario()
            layoutLista.visibility      = View.GONE
            layoutFormulario.visibility = View.VISIBLE
        }

        btnVolver.setOnClickListener {
            layoutFormulario.visibility = View.GONE
            layoutLista.visibility      = View.VISIBLE
            cargarListaPagos()
        }

        btnBuscarCliente.setOnClickListener {
            buscarClientePorTexto()
        }
        etId.setOnKeyListener { _, keyCode, event ->
            if (event.action == android.view.KeyEvent.ACTION_DOWN &&
                (keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                        keyCode == android.view.KeyEvent.KEYCODE_SEARCH)) {
                buscarClientePorTexto()
                true
            } else false
        }

        btnPagar.setOnClickListener {
            registrarPago()
        }

        btnVolverPrincipal.setOnClickListener {
            finish()
        }
    }

    private fun buscarClientePorTexto() {
        val texto = etId.text.toString().trim()

        if (texto.isEmpty()) {
            Toast.makeText(this, "Ingresá un ID o nombre para buscar", Toast.LENGTH_SHORT).show()
            return
        }

        val db = BDatos(this).readableDatabase
        var encontrado = false
        val idNumerico = texto.toIntOrNull()

        if (idNumerico != null) {
            val cursor = db.rawQuery(
                """SELECT p.PersonaId, p.Nombre, p.Apellido
                   FROM Persona p
                   INNER JOIN Socio s ON p.PersonaId = s.SocioId
                   WHERE s.SocioId = ?""",
                arrayOf(idNumerico.toString())
            )
            if (cursor.moveToFirst()) {
                completarFormulario(
                    id       = cursor.getInt(0),
                    nombre   = cursor.getString(1),
                    apellido = cursor.getString(2),
                    tipo     = "Socio"
                )
                encontrado = true
            }
            cursor.close()
        }

        if (!encontrado && idNumerico != null) {
            val cursor = db.rawQuery(
                """SELECT p.PersonaId, p.Nombre, p.Apellido
                   FROM Persona p
                   INNER JOIN NoSocio ns ON p.PersonaId = ns.NoSocioId
                   WHERE ns.NoSocioId = ?""",
                arrayOf(idNumerico.toString())
            )
            if (cursor.moveToFirst()) {
                completarFormulario(
                    id       = cursor.getInt(0),
                    nombre   = cursor.getString(1),
                    apellido = cursor.getString(2),
                    tipo     = "No Socio"
                )
                encontrado = true
            }
            cursor.close()
        }

        if (!encontrado) {
            val cursor = db.rawQuery(
                """SELECT p.PersonaId, p.Nombre, p.Apellido
                   FROM Persona p
                   INNER JOIN Socio s ON p.PersonaId = s.SocioId
                   WHERE LOWER(p.Nombre) LIKE LOWER(?) OR LOWER(p.Apellido) LIKE LOWER(?)
                   LIMIT 1""",
                arrayOf("%$texto%", "%$texto%")
            )
            if (cursor.moveToFirst()) {
                completarFormulario(
                    id       = cursor.getInt(0),
                    nombre   = cursor.getString(1),
                    apellido = cursor.getString(2),
                    tipo     = "Socio"
                )
                encontrado = true
            }
            cursor.close()
        }

        if (!encontrado) {
            val cursor = db.rawQuery(
                """SELECT p.PersonaId, p.Nombre, p.Apellido
                   FROM Persona p
                   INNER JOIN NoSocio ns ON p.PersonaId = ns.NoSocioId
                   WHERE LOWER(p.Nombre) LIKE LOWER(?) OR LOWER(p.Apellido) LIKE LOWER(?)
                   LIMIT 1""",
                arrayOf("%$texto%", "%$texto%")
            )
            if (cursor.moveToFirst()) {
                completarFormulario(
                    id       = cursor.getInt(0),
                    nombre   = cursor.getString(1),
                    apellido = cursor.getString(2),
                    tipo     = "No Socio"
                )
                encontrado = true
            }
            cursor.close()
        }

        db.close()

        if (!encontrado) {
            tvClienteEncontrado.visibility = View.GONE
            etNombre.setText("")
            etApellido.setText("")
            idEncontrado = -1
            tipoClienteEncontrado = ""
            Toast.makeText(this, "No se encontró ningún cliente con \"$texto\"", Toast.LENGTH_SHORT).show()
        }
    }

    private fun completarFormulario(id: Int, nombre: String, apellido: String, tipo: String) {
        idEncontrado = id
        tipoClienteEncontrado = tipo
        etNombre.setText(nombre)
        etApellido.setText(apellido)
        spinnerTipoCliente.setSelection(if (tipo == "Socio") 0 else 1)
        tvClienteEncontrado.text = "✓ $nombre $apellido — $tipo (ID: $id)"
        tvClienteEncontrado.visibility = View.VISIBLE
    }

    private fun registrarPago() {
        if (idEncontrado == -1) {
            Toast.makeText(this, "Buscá un cliente primero con la lupa 🔍", Toast.LENGTH_SHORT).show()
            return
        }

        val importe = etImporte.text.toString().trim().toDoubleOrNull()
        if (importe == null || importe <= 0) {
            Toast.makeText(this, "Ingresá un importe válido", Toast.LENGTH_SHORT).show()
            return
        }

        val medioPago = spinnerMedioPago.selectedItem.toString()
        val tipoPago  = spinnerTipoPago.selectedItem.toString()

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
        val cal  = Calendar.getInstance()
        val mes  = cal.get(Calendar.MONTH) + 1
        val anio = cal.get(Calendar.YEAR)

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
        val cantCuotas = when (medioPago) {
            "Tarjeta (3 pagos)" -> 3
            "Tarjeta (6 pagos)" -> 6
            else -> 1
        }

        if (cantCuotas == 1) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance().apply { set(anio, mes - 1, 1) }
            val fechaVenc = sdf.format(cal.time)

            val resultado = pagoCuotaDAO.insertarPagoCuota(
                socioId          = idEncontrado,
                fechaVencimiento = fechaVenc,
                montoCuota       = importe,
                medioPago        = medioPago,
                mesCuota         = mes,
                anioCuota        = anio
            )
            if (resultado != -1L) {
                mostrarComprobante("Cuota mensual ($mes/$anio)", importe, medioPago, "N° $resultado")
            } else {
                Toast.makeText(this, "Error al registrar el pago", Toast.LENGTH_SHORT).show()
            }
        } else {
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
                    "Cuota ${i + 1}: ${c.mesCuota}/${c.anioCuota} — $${"%.2f".format(c.montoCuota)}"
                }.joinToString("\n")

                mostrarComprobantePlan(cantCuotas, monto = importe, medioPago, detalle, planId = "Plan N° $idEncontrado")
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
            val nombreActividad = spinnerActividad.selectedItem.toString()
            mostrarComprobante("Actividad: $nombreActividad", importe, medioPago, "N° $resultado")
        } else {
            Toast.makeText(this, "Error al registrar el pago", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarComprobante(tipo: String, monto: Double, medio: String, comprobanteId: String) {
        val cliente = "${etNombre.text} ${etApellido.text}"

        AlertDialog.Builder(this)
            .setTitle("Pago registrado")
            .setMessage("Tipo: $tipo\nMonto: $${"%.2f".format(monto)}\nMedio: $medio\n\nComprobante $comprobanteId")
            .setPositiveButton("OK") { _, _ ->
                cerrarFormularioYRefrescar()
            }
            .setNeutralButton("Descargar Comprobante") { _, _ ->
                descargarComprobantePdf(cliente, tipo, monto, medio, comprobanteId)
                cerrarFormularioYRefrescar()
            }
            .show()
    }

    private fun mostrarComprobantePlan(cantCuotas: Int, monto: Double, medio: String, detalle: String, planId: String) {
        val cliente = "${etNombre.text} ${etApellido.text}"

        AlertDialog.Builder(this)
            .setTitle("Plan registrado exitosamente")
            .setMessage("Total: $${"%.2f".format(monto)}\nMedio: $medio\n\n$detalle")
            .setPositiveButton("OK") { _, _ ->
                cerrarFormularioYRefrescar()
            }
            .setNeutralButton("Descargar Comprobante") { _, _ ->
                descargarComprobantePdf(cliente, "Plan de $cantCuotas Cuotas", monto, medio, planId, detalle)
                cerrarFormularioYRefrescar()
            }
            .show()
    }

    private fun cerrarFormularioYRefrescar() {
        layoutFormulario.visibility = View.GONE
        layoutLista.visibility      = View.VISIBLE
        cargarListaPagos()
    }

    private fun descargarComprobantePdf(
        cliente: String,
        tipo: String,
        monto: Double,
        medio: String,
        idDoc: String,
        detallePlan: String = ""
    ) {
        val documentoPdf = PdfDocument()
        val altoPagina = if (detallePlan.isNotEmpty()) 450 else 320
        val infoPagina = PdfDocument.PageInfo.Builder(450, altoPagina, 1).create()
        val pagina = documentoPdf.startPage(infoPagina)
        val canvas: Canvas = pagina.canvas

        val pincelTitulo = Paint().apply {
            color = Color.parseColor("#A61B00")
            textSize = 22f
            isFakeBoldText = true
        }
        val pincelSubtitulo = Paint().apply { color = Color.BLACK; textSize = 15f; isFakeBoldText = true }
        val pincelTexto = Paint().apply { color = Color.BLACK; textSize = 14f }
        val pincelMonto = Paint().apply { color = Color.parseColor("#4CAF50"); textSize = 18f; isFakeBoldText = true }
        val pincelLinea = Paint().apply { color = Color.LTGRAY; strokeWidth = 2f }

        val hoy = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        canvas.drawText("CLUB DEPORTIVO", 30f, 40f, pincelTitulo)
        canvas.drawText("Comprobante de Pago Oficial", 30f, 65f, pincelSubtitulo)
        canvas.drawText("Fecha de emisión: $hoy", 30f, 85f, pincelTexto)
        canvas.drawLine(30f, 100f, 420f, 100f, pincelLinea)

        canvas.drawText("Cliente: $cliente", 30f, 130f, pincelTexto)
        canvas.drawText("Concepto: $tipo", 30f, 155f, pincelTexto)
        canvas.drawText("Medio de Pago: $medio", 30f, 180f, pincelTexto)

        canvas.drawLine(30f, 200f, 420f, 200f, pincelLinea)

        if (detallePlan.isNotEmpty()) {
            canvas.drawText("Desglose del plan autorizado:", 30f, 225f, pincelSubtitulo)
            var ejeY = 250f
            detallePlan.split("\n").forEach { linea ->
                canvas.drawText(linea, 40f, ejeY, pincelTexto)
                ejeY += 22f
            }
            canvas.drawLine(30f, ejeY, 420f, ejeY, pincelLinea)
            canvas.drawText("TOTAL PAGADO: $${"%.2f".format(monto)}", 30f, ejeY + 35f, pincelMonto)
            canvas.drawText(idDoc, 280f, ejeY + 35f, pincelSubtitulo)
        } else {
            canvas.drawText("TOTAL PAGADO: $${"%.2f".format(monto)}", 30f, 235f, pincelMonto)
        }

        documentoPdf.finishPage(pagina)

        val nombreArchivo = "Comprobante_${idDoc.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        var outputStream: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri: Uri? = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    outputStream = contentResolver.openOutputStream(uri)
                }
            } else {
                val carpetaDescargas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val archivoFinal = File(carpetaDescargas, nombreArchivo)
                outputStream = FileOutputStream(archivoFinal)
            }

            if (outputStream != null) {
                documentoPdf.writeTo(outputStream)
                Toast.makeText(this, "¡Comprobante PDF guardado en Descargas!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "No se pudo abrir el canal de salida", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error de escritura al guardar el documento", Toast.LENGTH_SHORT).show()
        } finally {
            try { outputStream?.close() } catch (e: IOException) { e.printStackTrace() }
            documentoPdf.close()
        }
    }

    private fun cargarListaPagos() {
        val pagos = mutableListOf<Map<String, String>>()
        pagos.addAll(pagoCuotaDAO.listarPagosConDetalle())
        pagos.addAll(pagoDiarioDAO.listarPagosDiariosConDetalle())
        val ordenados = pagos.sortedByDescending { it["fecha"] }

        if (ordenados.isEmpty()) {
            recyclerPagos.visibility = View.GONE
            tvSinPagos.visibility    = View.VISIBLE
        } else {
            tvSinPagos.visibility    = View.GONE
            recyclerPagos.visibility = View.VISIBLE
            recyclerPagos.layoutManager = LinearLayoutManager(this)

            // INTEGRACIÓN DEL CLICK: Capturamos los datos del mapa histórico al pulsar el item
            recyclerPagos.adapter = PagoListaAdapter(ordenados) { pago ->
                val cliente = "${pago["nombre"]} ${pago["apellido"]}"
                val tipo = if (pago["tipo"] == "Cuota") "Cuota Mensual" else "Actividad: ${pago["actividad"]}"
                val monto = pago["monto"]?.toDoubleOrNull() ?: 0.0
                val medio = pago["medioPago"] ?: "Efectivo"
                val idDoc = "N° ${pago["id"] ?: "0"}"

                // Dispara la descarga directa
                descargarComprobantePdf(cliente, tipo, monto, medio, idDoc)
            }
        }
    }

    private fun limpiarFormulario() {
        etId.text?.clear()
        etNombre.setText("")
        etApellido.setText("")
        etImporte.text?.clear()
        spinnerTipoPago.setSelection(0)
        spinnerMedioPago.setSelection(0)
        layoutActividad.visibility      = View.GONE
        tvClienteEncontrado.visibility  = View.GONE
        idEncontrado = -1
        tipoClienteEncontrado = ""
    }
}

// ---- ADAPTER DE LA LISTA DE PAGOS MODIFICADO ----
class PagoListaAdapter(
    private val pagos: List<Map<String, String>>,
    private val onComprobanteClick: (Map<String, String>) -> Unit // Evento lambda integrado
) : RecyclerView.Adapter<PagoListaAdapter.PagoViewHolder>() {

    inner class PagoViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvFecha:  TextView = v.findViewById(R.id.tvFechaPago)
        val tvNombre: TextView = v.findViewById(R.id.tvNombrePago)
        val tvMonto:  TextView = v.findViewById(R.id.tvMontoPago)
        val tvTipo:   TextView = v.findViewById(R.id.tvTipoPago)
        val rootLayout: View = v
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

        // Al presionar la fila, ejecuta la callback que llama a descargarComprobantePdf
        h.rootLayout.setOnClickListener {
            onComprobanteClick(p)
        }
    }

    override fun getItemCount() = pagos.size
}