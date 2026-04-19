package com.grupo4.clubdeportivo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.grupo4.clubdeportivo.adapters.ActividadAdapter
import com.grupo4.clubdeportivo.database.dao.ActividadDAO

class ActividadesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ActividadAdapter
    private lateinit var db: ActividadDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividades)

        db = ActividadDAO(this)
        recyclerView = findViewById(R.id.rvActividades)
        recyclerView.layoutManager = LinearLayoutManager(this)

        cargarActividades()

        val btnAgregarActividad = findViewById<FloatingActionButton>(R.id.btnNuevaActividad)

        btnAgregarActividad.setOnClickListener {
            val aNuevaActividad = Intent(this, NuevaActividadActivity::class.java)
            startActivity(aNuevaActividad)
        }
    }

    private fun cargarActividades() {
        val listaActividades = db.listarActividades()
        adapter = ActividadAdapter(listaActividades)
        recyclerView.adapter = adapter
    }

}