package com.grupo4.clubdeportivo

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.grupo4.clubdeportivo.adapters.SocioAdapter
import com.grupo4.clubdeportivo.database.dao.SocioDAO
import com.grupo4.clubdeportivo.database.models.Socio

class HomeActivity : AppCompatActivity() {

    // Declaramos las vistas
    private lateinit var cvSocio: MaterialCardView
    private lateinit var cvActividad: MaterialCardView
    private lateinit var cvPago: MaterialCardView
    private lateinit var cvListado: MaterialCardView
    private lateinit var btnMenu: ImageButton
    private lateinit var btnBuscar: ImageButton

    private lateinit var socioAdapter: SocioAdapter
    private var listaSociosCompleta: List<Socio> = listOf()

    // El DAO para conectar con la Base de Datos SQLite
    private lateinit var socioDAO: SocioDAO


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Inicializamos el DAO antes de preparar los datos
        socioDAO = SocioDAO(this)

        inicializarVistas()
        prepararDatosYAdapter()
        configurarEventos()
    }

    override fun onResume() {
        super.onResume()

        // Actualizamos la lista de socios Corremos el proceso automático para actualizar los estados según las fechas de vencimiento
        val morososNuevos = socioDAO.actualizarSociosAMorososAutomatico()
        if (morososNuevos > 0) {
            android.util.Log.d("ClubDeportivo", "Se detectaron $morososNuevos nuevos socios morosos.")
        }

        // Recargamos la lista por si hubo altas, ediciones o bajas en otras pantallas
        actualizarDatosLista()
    }

    private fun inicializarVistas() {
        cvSocio = findViewById(R.id.card_socios)
        cvActividad = findViewById(R.id.card_actividades)
        cvPago = findViewById(R.id.card_pagos)
        cvListado = findViewById(R.id.card_listado)
        btnMenu = findViewById(R.id.btnMenu)
        btnBuscar = findViewById(R.id.btnBuscar)
    }

    private fun prepararDatosYAdapter() {
        // Traemos los datos reales de la base de datos local
        listaSociosCompleta = socioDAO.obtenerTodos()

        // El adapter inicia vacío hasta que el usuario digite caracteres en el cuadro de búsqueda
        socioAdapter = SocioAdapter(
            listaActual = listOf(),
            onItemClick = { socio -> abrirDetalle(socio) },
            onMenuClick = { socio, view -> mostrarMenuOpciones(socio, view) }
        )
    }

    private fun configurarEventos() {
        cvActividad.setOnClickListener { startActivity(Intent(this, ActividadesActivity::class.java)) }
        cvPago.setOnClickListener { startActivity(Intent(this, PagosActivity::class.java)) }
        cvListado.setOnClickListener { startActivity(Intent(this, ListadoActivity::class.java)) }
        cvSocio.setOnClickListener { startActivity(Intent(this, SocioActivity::class.java)) }

        btnMenu.setOnClickListener { mostrarMenuLateral() }
        btnBuscar.setOnClickListener { mostrarBuscador() }
    }

    private fun abrirDetalle(socio: Socio) {
        val intent = Intent(this, DetalleSocioActivity::class.java)
        intent.putExtra(DetalleSocioActivity.EXTRA_SOCIO_ID, socio.idSocio)
        startActivity(intent)
    }

    // NUEVO: Método idéntico al de SocioActivity para inflar y controlar las acciones de los 3 puntos
    private fun mostrarMenuOpciones(socio: Socio, anchorView: android.view.View) {
        val popupMenu = PopupMenu(this, anchorView)
        popupMenu.menuInflater.inflate(R.menu.menu_opciones_socio, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menuEditar -> {
                    abrirDetalle(socio)
                    true
                }
                R.id.menuVerCarnet -> {
                    val intent = Intent(this, CarnetActivity::class.java)
                    intent.putExtra("EXTRA_SOCIO_ID", socio.idSocio)
                    startActivity(intent)
                    true
                }
                R.id.menuDarDeBaja -> {
                    confirmarBaja(socio)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    // NUEVO: Confirmación de baja desde el buscador de la Home
    private fun confirmarBaja(socio: Socio) {
        AlertDialog.Builder(this)
            .setTitle("Dar de baja")
            .setMessage("¿Querés dar de baja a ${socio.nombre} ${socio.apellido}?")
            .setPositiveButton("Confirmar") { _, _ ->
                val exito = socioDAO.darDeBaja(socio.idSocio)
                if (exito) {
                    Toast.makeText(this, "Socio dado de baja", Toast.LENGTH_SHORT).show()
                    actualizarDatosLista() // Recargamos la lista local y vaciamos la búsqueda actual para evitar inconsistencias
                    socioAdapter.actualizarLista(listOf())
                } else {
                    Toast.makeText(this, "Error al dar de baja", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarDatosLista() {
        listaSociosCompleta = socioDAO.obtenerTodos()
    }

    private fun mostrarBuscador() {
        val dialog = Dialog(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        val view = layoutInflater.inflate(R.layout.dialog_buscar, null)
        dialog.setContentView(view)

        val etInput = view.findViewById<EditText>(R.id.etInputBusqueda)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBackSearch)
        val btnClear = view.findViewById<ImageButton>(R.id.btnClearSearch)

        // Inicializamos el RecyclerView interno de dialog buscar pasándole la vista inflada
        val rvResultadosDialog = view.findViewById<RecyclerView>(R.id.rvResultadosBusqueda)
        rvResultadosDialog.layoutManager = LinearLayoutManager(this)
        rvResultadosDialog.adapter = socioAdapter

        etInput.requestFocus()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        btnBack.setOnClickListener {
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
                    rvResultadosDialog.visibility = View.GONE
                    socioAdapter.actualizarLista(listOf())
                } else {
                    rvResultadosDialog.visibility = View.VISIBLE

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

        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            params.gravity = android.view.Gravity.START
            params.width = (resources.displayMetrics.widthPixels * 0.7).toInt()
            params.height = WindowManager.LayoutParams.MATCH_PARENT
            window.attributes = params
        }

        val nombreUsuario = intent.getStringExtra("USUARIO_NOMBRE") ?: "Invitado"
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreUsuarioMenu)
        tvNombre.text = nombreUsuario

        view.findViewById<LinearLayout>(R.id.btnCerrarSesion).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            dialog.dismiss()
        }

        dialog.show()
    }
}