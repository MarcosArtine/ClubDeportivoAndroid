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
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.grupo4.clubdeportivo.database.dao.SocioDAO
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class CarnetActivity : AppCompatActivity() {

    private lateinit var socioDAO: SocioDAO

    private var nombreCompleto = ""
    private var dniSocio = ""
    private var numeroSocioStr = ""
    private var nombreArchivoPdf = "Carnet_Socio"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carnet)

        socioDAO = SocioDAO(this)

        val btnAtras = findViewById<ImageButton>(R.id.btnAtrasCarnet)
        val tvNombre = findViewById<TextView>(R.id.tvNombreCarnet)
        val tvDni = findViewById<TextView>(R.id.tvDniCarnet)
        val tvNumeroSocio = findViewById<TextView>(R.id.tvNumeroCarnetTarjeta)
        val btnDescargar = findViewById<MaterialButton>(R.id.btnCompartirCarnet) // Sigue siendo tu botón principal

        val socioId = intent.getIntExtra("EXTRA_SOCIO_ID", -1)

        if (socioId != -1) {
            val socio = socioDAO.obtenerPorId(socioId)
            if (socio != null) {
                nombreCompleto = "${socio.nombre} ${socio.apellido}"
                dniSocio = "DNI: ${socio.nroDni}"

                val ultimosDigitos = socio.nroCarnet.takeLast(4).padStart(4, '0')
                numeroSocioStr = "Socio N° $ultimosDigitos"

                nombreArchivoPdf = "Carnet_${socio.nombre}_${socio.apellido}".replace(" ", "_")

                tvNombre.text = nombreCompleto
                tvDni.text = dniSocio
                tvNumeroSocio.text = numeroSocioStr
            }
        }

        btnAtras.setOnClickListener { finish() }


        btnDescargar.setOnClickListener {
            if (nombreCompleto.isNotEmpty()) {
                descargarPdfEnCelular()
            } else {
                Toast.makeText(this, "No hay datos de socio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun descargarPdfEnCelular() {
        // Creamos el PDF en memoria
        val documentoPdf = PdfDocument()
        val infoPagina = PdfDocument.PageInfo.Builder(400, 250, 1).create()
        val pagina = documentoPdf.startPage(infoPagina)
        val canvas: Canvas = pagina.canvas

        // Configuración de estilos de texto (Paints)
        val pincelTitulo = Paint().apply {
            color = Color.parseColor("#A61B00")
            textSize = 20f
            isFakeBoldText = true
        }
        val pincelTexto = Paint().apply { color = Color.BLACK; textSize = 16f }
        val pincelNumero = Paint().apply { color = Color.DKGRAY; textSize = 18f; isFakeBoldText = true }
        val pincelLinea = Paint().apply { color = Color.LTGRAY; strokeWidth = 2f }

        // Dibujamos el contenido
        canvas.drawText("CLUB DEPORTIVO", 30f, 40f, pincelTitulo)
        canvas.drawText("Credencial Digital de Socio", 30f, 65f, pincelTexto)
        canvas.drawLine(30f, 80f, 370f, 80f, pincelLinea)
        canvas.drawText("Nombre: $nombreCompleto", 30f, 120f, pincelTexto)
        canvas.drawText(dniSocio, 30f, 150f, pincelTexto)
        canvas.drawLine(30f, 180f, 370f, 180f, pincelLinea)
        canvas.drawText(numeroSocioStr, 30f, 215f, pincelNumero)

        documentoPdf.finishPage(pagina)

        // Guardamos el archivo usando MediaStore
        val nombreFinalConExtension = "$nombreArchivoPdf.pdf"
        var outputStream: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Lógica para Android 10 o superior
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, nombreFinalConExtension)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    outputStream = resolver.openOutputStream(uri)
                }
            } else {
                val carpetaDescargas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val archivoFinal = File(carpetaDescargas, nombreFinalConExtension)
                outputStream = FileOutputStream(archivoFinal)
            }

            if (outputStream != null) {
                documentoPdf.writeTo(outputStream)
                Toast.makeText(this, "¡PDF Guardado en Descargas!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Error al abrir el canal de guardado", Toast.LENGTH_SHORT).show()
            }

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al descargar el archivo", Toast.LENGTH_SHORT).show()
        } finally {
            try {
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            documentoPdf.close()
        }
    }
}