package com.example.edugtapp.ui.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.edugtapp.R
import com.example.edugtapp.network.NotaService
import org.json.JSONObject

class ActividadAdapter(
    private val actividades: MutableList<JSONObject>,
    private val cuiEstudiante: String
) : RecyclerView.Adapter<ActividadAdapter.ActividadViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActividadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actividad_con_nota, parent, false)
        return ActividadViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActividadViewHolder, position: Int) {
        val actividad = actividades[position]
        val context = holder.itemView.context

        holder.tvNombre.text = actividad.getString("nombre")
        holder.tvTipo.text = context.getString(R.string.tipo_formato, actividad.getString("tipo"))
        holder.tvPonderacion.text = context.getString(R.string.ponderacion_formato, actividad.getDouble("ponderacion"))

        val nota = actividad.optDouble("nota", -1.0)
        if (nota >= 0) {
            holder.tvNota.visibility = View.VISIBLE
            holder.tvNota.text = context.getString(R.string.nota_obtenida_formato, nota)
        } else {
            holder.tvNota.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            mostrarDialogoCalificacion(context, actividad, cuiEstudiante) {
                actividad.put("nota", it) // actualizar nota localmente
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int = actividades.size

    class ActividadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvTipo: TextView = view.findViewById(R.id.tvTipo)
        val tvPonderacion: TextView = view.findViewById(R.id.tvPonderacion)
        val tvNota: TextView = view.findViewById(R.id.tvNota)
    }

    private fun mostrarDialogoCalificacion(context: Context, actividad: JSONObject, cuiEstudiante: String, onNotaGuardada: (Double) -> Unit) {
        val editText = EditText(context)
        editText.hint = "Ingrese nota (ej. 8.5)"
        editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

        val evaluacionId = actividad.getInt("evaluacionId")
        val notaExistente = actividad.optDouble("nota", -1.0)
        if (notaExistente >= 0) {
            editText.setText(notaExistente.toString())
        }

        AlertDialog.Builder(context)
            .setTitle(if (notaExistente >= 0) "Actualizar Nota" else "Calificar Actividad")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val notaTexto = editText.text.toString()
                val valor = notaTexto.toDoubleOrNull()

                if (valor != null && valor in 0.0..100.0) {
                    val notaJson = JSONObject().apply {
                        put("evaluacion", JSONObject().put("id", evaluacionId))
                        put("estudiante", JSONObject().put("cui", cuiEstudiante))
                        put("nota", valor)
                    }

                    NotaService.enviarNota(notaJson) { exito ->
                        (context as? AppCompatActivity)?.runOnUiThread {
                            if (exito) {
                                Toast.makeText(context, "Nota guardada", Toast.LENGTH_SHORT).show()
                                onNotaGuardada(valor)
                            } else {
                                Toast.makeText(context, "Error al guardar nota", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Ingrese una nota válida entre 0 y 100", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
