package com.grupo4.clubdeportivo

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.clubdeportivo.adapters.SocioAdapter
import com.grupo4.clubdeportivo.database.dao.SocioDAO
import com.grupo4.clubdeportivo.database.models.Socio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListadoActivity : AppCompatActivity() {

    private lateinit var rvMorosos: RecyclerView
    private lateinit var btnVolver: ImageButton
    private lateinit var btnDescargar: ImageButton

    private lateinit var socioDAO: SocioDAO
    private lateinit var adapter: SocioAdapter

    private var listaMorosos: List<Socio> = emptyList()

    // Ahora registramos el contrato para generar un "application/pdf"
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { exportarListaAPdf(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listado)

        socioDAO = SocioDAO(this)
        rvMorosos = findViewById(R.id.rvListadoMorosos)
        btnVolver = findViewById(R.id.btnVolver)
        btnDescargar = findViewById(R.id.btnDescargar)

        configurarRecyclerView()
        cargarSociosMorosos()

        btnVolver.setOnClickListener {
            finish()
        }

        btnDescargar.setOnClickListener {
            if (listaMorosos.isEmpty()) {
                Toast.makeText(this, "No hay socios morosos para exportar", Toast.LENGTH_SHORT).show()
            } else {
                // CAMBIADO: Sugerimos la extensión .pdf
                createDocumentLauncher.launch("socios_morosos_${System.currentTimeMillis()}.pdf")
            }
        }
    }

    private fun configurarRecyclerView() {
        adapter = SocioAdapter(
            listaActual = emptyList(),
            onItemClick = { /* Acción opcional */ },
            onMenuClick = { _, _ -> }
        )
        rvMorosos.layoutManager = LinearLayoutManager(this)
        rvMorosos.adapter = adapter
    }

    private fun cargarSociosMorosos() {
        listaMorosos = socioDAO.obtenerMorosos()

        if (listaMorosos.isEmpty()) {
            val listaCompleta = socioDAO.obtenerTodos()
            listaMorosos = listaCompleta.filter {
                it.estadoSocio.equals("Moroso", ignoreCase = true)
            }
        }
        adapter.actualizarLista(listaMorosos)
    }

    // lógica de exportación a PDF nativo
    private fun exportarListaAPdf(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val pdfDocument = PdfDocument()

                    // Configuración de página A4 estándar (595 x 842 píxeles)
                    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas: Canvas = page.canvas

                    // Configuración de estilos de texto
                    val tituloPaint = Paint().apply {
                        color = Color.rgb(40, 40, 40)
                        textSize = 20f
                        isFakeBoldText = true
                    }

                    val encabezadoPaint = Paint().apply {
                        color = Color.rgb(100, 100, 100)
                        textSize = 12f
                        isFakeBoldText = true
                    }

                    val textoPaint = Paint().apply {
                        color = Color.BLACK
                        textSize = 11f
                    }

                    val lineaPaint = Paint().apply {
                        color = Color.LTGRAY
                        strokeWidth = 1f
                    }

                    // Dibujamos el encabezado del documento
                    canvas.drawText("CLUB DEPORTIVO - REPORTE DE MOROSOS", 40f, 60f, tituloPaint)

                    // Dibujamos las columnas de la tabla
                    var yPos = 110f
                    canvas.drawText("Nombre", 40f, yPos, encabezadoPaint)
                    canvas.drawText("Apellido", 160f, yPos, encabezadoPaint)
                    canvas.drawText("Documento", 300f, yPos, encabezadoPaint)
                    canvas.drawText("Estado", 440f, yPos, encabezadoPaint)

                    // Línea divisoria horizontal inferior
                    canvas.drawLine(40f, yPos + 10f, 550f, yPos + 10f, lineaPaint)

                    yPos += 35f

                    // Dibujamos cada socio moroso en un renglón diferente
                    listaMorosos.forEach { socio ->
                        canvas.drawText(socio.nombre, 40f, yPos, textoPaint)
                        canvas.drawText(socio.apellido, 160f, yPos, textoPaint)
                        canvas.drawText(socio.nroDni, 300f, yPos, textoPaint)
                        canvas.drawText(socio.estadoSocio, 440f, yPos, textoPaint)

                        yPos += 25f // Espaciado vertical entre filas

                        // Si la lista supera el tamaño de la hoja A4, detenemos el dibujado de esta página
                        if (yPos > 800f) {
                            return@forEach
                        }
                    }

                    pdfDocument.finishPage(page)
                    pdfDocument.writeTo(outputStream)
                    pdfDocument.close()
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ListadoActivity, "PDF guardado con éxito", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ListadoActivity, "Error al generar el PDF", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}