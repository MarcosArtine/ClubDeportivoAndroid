package com.grupo4.clubdeportivo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.clubdeportivo.R
import com.grupo4.clubdeportivo.database.models.Socio

class SocioAdapter(
    private var listaActual: List<Socio>,
    private val onItemClick: (Socio) -> Unit,
    private val onMenuClick: (Socio, View) -> Unit
) : RecyclerView.Adapter<SocioAdapter.SocioViewHolder>() {

    class SocioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumeroSocio: TextView = view.findViewById(R.id.tvNumeroSocio)
        val tvNombreSocio: TextView = view.findViewById(R.id.tvNombreSocio)
        val tvEstadoSocio: TextView = view.findViewById(R.id.tvEstadoSocio)
        val btnMenu: ImageButton = view.findViewById(R.id.btnEditar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SocioViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_socio, parent, false)
        return SocioViewHolder(vista)
    }

    override fun getItemCount(): Int = listaActual.size

    override fun onBindViewHolder(holder: SocioViewHolder, position: Int) {
        val socio = listaActual[position]

        holder.tvNumeroSocio.text = "N° ${socio.nroCarnet.takeLast(4).padStart(4, '0')}"

        holder.tvNombreSocio.text = "${socio.nombre} ${socio.apellido}"

        // Estado con color según su valor (Activo=verde, Moroso=rojo, Inactivo=gris)
        holder.tvEstadoSocio.text = socio.estadoSocio
        val colorEstado = when (socio.estadoSocio) {
            "Activo" -> android.graphics.Color.parseColor("#4CAF50")   // Verde
            "Moroso" -> android.graphics.Color.parseColor("#F44336")   // Rojo
            else     -> android.graphics.Color.parseColor("#9E9E9E")   // Gris (Inactivo)
        }
        holder.tvEstadoSocio.setTextColor(colorEstado)

        holder.itemView.setOnClickListener { onItemClick(socio) }

        holder.btnMenu.setOnClickListener { view -> onMenuClick(socio, view) }
    }

    fun actualizarLista(nuevaLista: List<Socio>) {
        listaActual = nuevaLista
        notifyDataSetChanged()
    }
}