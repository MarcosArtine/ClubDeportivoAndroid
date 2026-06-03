package com.grupo4.clubdeportivo.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.grupo4.clubdeportivo.NuevaActividadActivity
import com.grupo4.clubdeportivo.R
import com.grupo4.clubdeportivo.database.models.Actividad

class ActividadAdapter(private val listaActividades: List<Actividad>) :
    RecyclerView.Adapter<ActividadAdapter.ActividadViewHolder>() {

    class ActividadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.tvNombreActividad)
        val monto: TextView = itemView.findViewById(R.id.tvMontoActividad)
        val icono: ImageView = itemView.findViewById(R.id.imgIcono)
        val btnEditar: ImageButton = itemView.findViewById(R.id.btnEditar) // ← Agregamos el botón editar
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActividadViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ActividadViewHolder(layoutInflater.inflate(R.layout.item_actividad, parent, false))
    }

    override fun getItemCount(): Int = listaActividades.size

    override fun onBindViewHolder(holder: ActividadViewHolder, position: Int) {
        val item = listaActividades[position]
        holder.nombre.text = item.nombreActividad
        holder.monto.text = "$ ${item.montoActividad}"

        if (!item.urlImagen.isNullOrEmpty()) {
            try {
                val uriImagen = Uri.parse(item.urlImagen)
                holder.icono.setImageURI(uriImagen)
            } catch (e: Exception) {
                holder.icono.setImageResource(R.drawable.ic_actividad)
            }
        } else {
            holder.icono.setImageResource(R.drawable.ic_actividad)
        }

        // Configuración del botón Editar
        holder.btnEditar.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, NuevaActividadActivity::class.java).apply {
                putExtra("MODO_EDITAR", true)
                putExtra("ACTIVIDAD_OBJETO", item)
            }
            context.startActivity(intent)
        }
    }
}