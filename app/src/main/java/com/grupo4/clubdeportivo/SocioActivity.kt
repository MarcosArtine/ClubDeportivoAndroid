package com.grupo4.clubdeportivo

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.grupo4.clubdeportivo.adapters.SocioAdapter
import com.grupo4.clubdeportivo.database.dao.SocioDAO
import com.grupo4.clubdeportivo.database.models.Socio

class SocioActivity : AppCompatActivity() {
    private lateinit var rvSocios: RecyclerView
    private lateinit var btnNuevoSocio: FloatingActionButton
    private lateinit var btnVolver: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var socioDAO: SocioDAO
    private lateinit var adapter: SocioAdapter
    private var listaTodos: List<Socio> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socio)

        socioDAO = SocioDAO(this)

        inicializarVistas()
        configurarRecyclerView()
        configurarBusqueda()

        btnNuevoSocio.setOnClickListener {
            val intent = Intent(this, NuevoSocioActivity::class.java)
            // startActivityForResult nos avisa cuando el usuario vuelve
            // así podemos recargar la lista si registró alguien nuevo
            startActivityForResult(intent, CODIGO_NUEVO_SOCIO)
        }

        // Flecha atrás → vuelve a la pantalla anterior
        btnVolver.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        cargarSocios()
    }

    // Inicializa las referencias a las vistas del XML
    private fun inicializarVistas() {
        rvSocios = findViewById(R.id.rvActividades)  // reusar el mismo id del layout existente
        btnNuevoSocio = findViewById(R.id.btnNuevoSocio)
        btnVolver = findViewById(R.id.btnVolver)
        searchView = findViewById(R.id.searchViewSocios)
    }

    // Configura el RecyclerView con su LayoutManager y Adapter
    private fun configurarRecyclerView() {
        adapter = SocioAdapter(
            listaActual = emptyList(),
            onItemClick = { socio -> abrirDetalle(socio) },
            onMenuClick = { socio, view -> mostrarMenuOpciones(socio, view) }
        )
        rvSocios.layoutManager = LinearLayoutManager(this)
        rvSocios.adapter = adapter
    }

    private fun configurarBusqueda() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val texto = newText?.trim() ?: ""
                if (texto.isEmpty()) {
                    adapter.actualizarLista(listaTodos)
                } else {
                    val filtrada = socioDAO.buscarPorNombreOApellido(texto)
                    adapter.actualizarLista(filtrada)
                }
                return true
            }
        })
    }

    private fun cargarSocios() {
        listaTodos = socioDAO.obtenerTodos()
        adapter.actualizarLista(listaTodos)
    }

    private fun abrirDetalle(socio: Socio) {
        val intent = Intent(this, DetalleSocioActivity::class.java)
        intent.putExtra(DetalleSocioActivity.EXTRA_SOCIO_ID, socio.idSocio)
        startActivity(intent)
    }

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
                    // En lugar de un diálogo, lanzamos la nueva pantalla estética
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


    private fun confirmarBaja(socio: Socio) {
        AlertDialog.Builder(this)
            .setTitle("Dar de baja")
            .setMessage("¿Querés dar de baja a ${socio.nombre} ${socio.apellido}?")
            .setPositiveButton("Confirmar") { _, _ ->
                val exito = socioDAO.darDeBaja(socio.idSocio)
                if (exito) {
                    Toast.makeText(this, "Socio dado de baja", Toast.LENGTH_SHORT).show()
                    cargarSocios() // Recargamos la lista
                } else {
                    Toast.makeText(this, "Error al dar de baja", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    @Deprecated("Usar ActivityResultLauncher en versiones futuras")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Cuando volvemos de la pantalla de registro, recargamos la lista
        if (requestCode == CODIGO_NUEVO_SOCIO && resultCode == RESULT_OK) {
            cargarSocios()
        }
    }

    companion object {
        // Código para identificar el resultado de NuevoSocioActivity
        // (podría ser cualquier número > 0, por convención usamos constantes)
        const val CODIGO_NUEVO_SOCIO = 100
    }
}