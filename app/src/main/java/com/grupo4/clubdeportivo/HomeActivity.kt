package com.grupo4.clubdeportivo

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.grupo4.clubdeportivo.adapters.SocioAdapter
import com.grupo4.clubdeportivo.database.models.Socio

class HomeActivity : AppCompatActivity() {

    private lateinit var cvSocio: MaterialCardView
    private lateinit var cvActividad: MaterialCardView
    private lateinit var cvPago: MaterialCardView
    private lateinit var cvListado: MaterialCardView
    private lateinit var btnMenu: ImageButton
    private lateinit var btnBuscar: ImageButton

    private lateinit var socioAdapter: SocioAdapter
    private var listaSociosCompleta: List<Socio> = listOf()
    private lateinit var rvResultados: RecyclerView

    private lateinit var scrollContenido: View



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        inicializarVistas()
        prepararDatosYAdapter()
        configurarEventos()
    }

    private fun inicializarVistas() {
        cvSocio = findViewById(R.id.card_socios)
        cvActividad = findViewById(R.id.card_actividades)
        cvPago = findViewById(R.id.card_pagos)
        cvListado = findViewById(R.id.card_listado)
        btnMenu = findViewById(R.id.btnMenu)
        btnBuscar = findViewById(R.id.btnBuscar)

        // El RecyclerView donde se verán los socios encontrados
        rvResultados = findViewById(R.id.rvResultadosBusqueda)
        rvResultados.layoutManager = LinearLayoutManager(this)

        scrollContenido = findViewById(R.id.scrollContenido)
    }

    private fun prepararDatosYAdapter() {
        // AQUÍ DEBES CARGAR TU LISTA DESDE LA BASE DE DATOS
        // Ejemplo: listaSociosCompleta = SocioDAO(this).obtenerTodos()
        listaSociosCompleta = listOf() // Reemplazar por datos reales

        socioAdapter = SocioAdapter(
            listaActual = listOf(), // Empezamos vacío hasta que el usuario escriba
            onItemClick = { socio ->
                // Acción al tocar el socio (ej: ir al detalle)
                val intent = Intent(this, DetalleSocioActivity::class.java)
                intent.putExtra("SOCIO_ID", socio.idSocio)
                startActivity(intent)
            },
            onMenuClick = { socio, view ->
                // Acción para el botón de tres puntos (editar/borrar)
            }
        )
        rvResultados.adapter = socioAdapter
    }

    private fun configurarEventos() {
        cvActividad.setOnClickListener { startActivity(Intent(this, ActividadesActivity::class.java)) }
        cvPago.setOnClickListener { startActivity(Intent(this, PagosActivity::class.java)) }
        cvListado.setOnClickListener { startActivity(Intent(this, ListadoActivity::class.java)) }
        cvSocio.setOnClickListener { startActivity(Intent(this, SocioActivity::class.java)) }

        btnMenu.setOnClickListener { mostrarMenuLateral() }

        // Evento para abrir el buscador
        btnBuscar.setOnClickListener { mostrarBuscador() }
    }

    private fun mostrarBuscador() {
        val dialog = Dialog(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        val view = layoutInflater.inflate(R.layout.dialog_buscar, null)
        dialog.setContentView(view)

        val etInput = view.findViewById<EditText>(R.id.etInputBusqueda)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBackSearch)
        val btnClear = view.findViewById<ImageButton>(R.id.btnClearSearch)

        etInput.requestFocus()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        btnBack.setOnClickListener {
            // Al cerrar, volvemos a mostrar las tarjetas y ocultamos la lista
            rvResultados.visibility = View.GONE
            scrollContenido.visibility = View.VISIBLE
            socioAdapter.actualizarLista(listOf())
            dialog.dismiss()
        }
        
        btnClear.setOnClickListener {
            etInput.text.clear()
        }

        etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase().trim()

                if (query.isEmpty()) {
                    // Si está vacío, ocultamos la lista blanca y mostramos tarjetas rojas
                    rvResultados.visibility = View.GONE
                    scrollContenido.visibility = View.VISIBLE
                    socioAdapter.actualizarLista(listOf())
                } else {
                    // Si hay texto, mostramos la lista blanca encima de todo
                    rvResultados.visibility = View.VISIBLE
                    scrollContenido.visibility = View.GONE

                    val filtrados = listaSociosCompleta.filter { socio ->
                        socio.nombre.lowercase().contains(query) ||
                                socio.apellido.lowercase().contains(query) ||
                                socio.nroCarnet.contains(query)
                    }
                    socioAdapter.actualizarLista(filtrados)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        dialog.show()
    }

    private fun mostrarMenuLateral() {
        val dialog = Dialog(this, R.style.MenuLateralDialog)
        val view = layoutInflater.inflate(R.layout.dialog_menu, null)
        dialog.setContentView(view)

        // Configurar dimensiones del diálogo lateral
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            params.gravity = android.view.Gravity.START
            params.width = (resources.displayMetrics.widthPixels * 0.7).toInt()
            params.height = WindowManager.LayoutParams.MATCH_PARENT
            window.attributes = params
        }

        // Recupera el nombre enviado por el Intent. si no viene nada, usamos "Invitado"
        val nombreUsuario = intent.getStringExtra("USUARIO_NOMBRE") ?: "Invitado"

        // Busca el TextView dentro de la vista inflada del diálogo y asignarle el nombre
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreUsuarioMenu)
        tvNombre.text = nombreUsuario

        // Configura el clic en el botón de cerrar sesión
        view.findViewById<LinearLayout>(R.id.btnCerrarSesion).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            dialog.dismiss()
        }

        dialog.show()
    }
}