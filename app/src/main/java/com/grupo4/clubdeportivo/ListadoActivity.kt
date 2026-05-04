package com.grupo4.clubdeportivo

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.clubdeportivo.adapters.SocioAdapter
import com.grupo4.clubdeportivo.database.dao.SocioDAO
import com.grupo4.clubdeportivo.database.models.Socio

class ListadoActivity : AppCompatActivity() {

    private lateinit var rvMorosos: RecyclerView
    private lateinit var btnVolver: ImageButton
    private lateinit var socioDAO: SocioDAO
    private lateinit var adapter: SocioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listado)

        socioDAO = SocioDAO(this)
        rvMorosos = findViewById(R.id.rvListadoMorosos)
        btnVolver = findViewById(R.id.btnVolver)

        configurarRecyclerView()

        cargarSociosMorosos()

        btnVolver.setOnClickListener {

        }
    }

    private fun configurarRecyclerView() {
        adapter = SocioAdapter(
            listaActual = emptyList(),
            onItemClick = { socio ->
                // Acción opcional al tocar un moroso
            },
            onMenuClick = { socio, view ->
                // Podemos dejarlo vacío o no mostrar el botón btnEditar en el item
            }
        )
        rvMorosos.layoutManager = LinearLayoutManager(this)
        rvMorosos.adapter = adapter
    }

    private fun cargarSociosMorosos() {

        val listaCompleta = socioDAO.obtenerTodos()


        val listaMorosos = listaCompleta.filter {
            it.estadoSocio.equals("Moroso", ignoreCase = true)
        }

        adapter.actualizarLista(listaMorosos)
    }
}