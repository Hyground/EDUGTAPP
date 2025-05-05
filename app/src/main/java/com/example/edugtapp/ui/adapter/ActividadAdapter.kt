package com.example.edugtapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.edugtapp.R
import org.json.JSONObject

class ActividadAdapter(private val actividades: List<JSONObject>) :
    RecyclerView.Adapter<ActividadAdapter.ActividadViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActividadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actividad, parent, false)
        return ActividadViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActividadViewHolder, position: Int) {
        val actividad = actividades[position]
        holder.tvNombre.text = actividad.getString("nombre")
        holder.tvTipo.text = "Tipo: ${actividad.getString("tipo")}"
        holder.tvPonderacion.text = "Ponderación: ${actividad.getDouble("ponderacion")}%"
    }

    override fun getItemCount(): Int = actividades.size

    class ActividadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvActividadNombre)
        val tvTipo: TextView = view.findViewById(R.id.tvActividadTipo)
        val tvPonderacion: TextView = view.findViewById(R.id.tvActividadPonderacion)
    }
}
