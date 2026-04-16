package com.grupo4.clubdeportivo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.clubdeportivo.R
import com.grupo4.clubdeportivo.database.models.Actividad

class ActividadAdapter(private val listaActividades: List<Actividad>) :
    RecyclerView.Adapter<ActividadAdapter.ActividadViewHolder>() {

    // El ViewHolder es el "contenedor" de la vista de cada fila
    class ActividadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tvNombreActividad)
        val monto: TextView = view.findViewById(R.id.tvMontoActividad)
    }

    // Define qué layout usar para cada fila
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActividadViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ActividadViewHolder(layoutInflater.inflate(R.layout.item_actividad, parent, false))
    }

    // Retorna el tamaño de la lista
    override fun getItemCount(): Int = listaActividades.size

    // Conecta los datos del objeto Actividad con los elementos visuales (TextViews)
    override fun onBindViewHolder(holder: ActividadViewHolder, position: Int) {
        val item = listaActividades[position]
        holder.nombre.text = item.nombreActividad
        holder.monto.text = "$ ${item.montoActividad}"
    }
}