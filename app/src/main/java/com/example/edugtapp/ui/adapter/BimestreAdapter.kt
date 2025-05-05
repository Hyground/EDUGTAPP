package com.example.edugtapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.edugtapp.R

class BimestreAdapter(
    private val bimestres: List<Pair<Int, String>>,
    private val onBimestreClick: (bimestreId: Int, nombreBimestre: String) -> Unit
) : RecyclerView.Adapter<BimestreAdapter.BimestreViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BimestreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_curso, parent, false)
        return BimestreViewHolder(view)
    }

    override fun onBindViewHolder(holder: BimestreViewHolder, position: Int) {
        val (id, nombre) = bimestres[position]
        holder.tvNombre.text = nombre.uppercase()
        holder.cardView.setOnClickListener {
            onBimestreClick(id, nombre)
        }
    }

    override fun getItemCount(): Int = bimestres.size

    class BimestreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCurso)
        val cardView: CardView = view.findViewById(R.id.cardCurso)
    }
}
