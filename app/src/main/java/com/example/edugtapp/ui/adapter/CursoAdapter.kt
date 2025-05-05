package com.example.edugtapp.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.edugtapp.R

class CursoAdapter(
    private val cursos: List<Pair<Int, String>>,
    private val onCursoClick: (cursoId: Int, nombreCurso: String) -> Unit
) : RecyclerView.Adapter<CursoAdapter.CursoViewHolder>() {

    private val colores = listOf(
        "#FFCDD2", "#F8BBD0", "#E1BEE7", "#D1C4E9",
        "#C5CAE9", "#B3E5FC", "#B2EBF2", "#C8E6C9",
        "#DCEDC8", "#FFF9C4", "#FFE0B2", "#FFCCBC"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_curso, parent, false)
        return CursoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CursoViewHolder, position: Int) {
        val (cursoId, nombreCurso) = cursos[position]
        holder.tvNombreCurso.text = "Calificar\n${nombreCurso.uppercase()}"
        holder.cardCurso.setCardBackgroundColor(Color.parseColor(colores[position % colores.size]))

        holder.cardCurso.setOnClickListener {
            onCursoClick(cursoId, nombreCurso)
        }
    }

    override fun getItemCount(): Int = cursos.size

    class CursoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombreCurso: TextView = view.findViewById(R.id.tvNombreCurso)
        val cardCurso: CardView = view.findViewById(R.id.cardCurso)
    }
}
