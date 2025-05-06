package com.example.edugtapp.ui.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
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
import kotlin.random.Random

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

        // Colores aleatorios en cada tarjeta
        holder.itemView.setBackgroundColor(generarColorAleatorio())

        holder.tvNombre.text = actividad.getString("nombre")
        holder.tvTipo.text = context.getString(R.string.tipo_formato, actividad.getString("tipo"))

        val ponderacion = actividad.getDouble("ponderacion")
        holder.tvPonderacion.text = context.getString(R.string.ponderacion_formato, ponderacion)

        val nota = actividad.optDouble("nota", -1.0)
        if (nota >= 0) {
            holder.tvNota.visibility = View.VISIBLE
            holder.tvNota.text = context.getString(R.string.nota_obtenida_formato, nota)
        } else {
            holder.tvNota.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            mostrarDialogoCalificacion(context, actividad, cuiEstudiante, ponderacion) {
                actividad.put("nota", it)
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

    private fun mostrarDialogoCalificacion(
        context: Context,
        actividad: JSONObject,
        cuiEstudiante: String,
        ponderacionMaxima: Double,
        onNotaGuardada: (Double) -> Unit
    ) {
        val editText = EditText(context).apply {
            hint = "Nota (máx. $ponderacionMaxima)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val evaluacionId = actividad.getInt("evaluacionId")
        val notaExistente = actividad.optDouble("nota", -1.0)
        if (notaExistente >= 0) {
            editText.setText(notaExistente.toString())
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(if (notaExistente >= 0) "Actualizar Nota" else "Calificar Actividad")
            .setView(editText)
            .setPositiveButton("Guardar", null) // Importante, null aquí
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            val buttonGuardar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            buttonGuardar.setOnClickListener {
                val notaTexto = editText.text.toString()
                val valor = notaTexto.toDoubleOrNull()

                if (valor != null && valor in 0.0..ponderacionMaxima) {
                    val notaJson = JSONObject().apply {
                        put("evaluacion", JSONObject().put("id", evaluacionId))
                        put("estudiante", JSONObject().put("cui", cuiEstudiante))
                        put("nota", valor)
                    }

                    NotaService.enviarNota(notaJson) { exito ->
                        (context as? AppCompatActivity)?.runOnUiThread {
                            if (exito) {
                                Toast.makeText(context, "Nota guardada correctamente.", Toast.LENGTH_SHORT).show()
                                onNotaGuardada(valor)
                                dialog.dismiss() // Ahora sí cierra el diálogo solo si tiene éxito
                            } else {
                                Toast.makeText(context, "Error al guardar la nota.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Nota no válida. Debe estar entre 0 y $ponderacionMaxima.",
                        Toast.LENGTH_SHORT
                    ).show()
                    editText.error = "Ingrese una nota válida"
                }
            }
        }

        dialog.show()
    }


    // Genera colores suaves aleatorios para las tarjetas
    private fun generarColorAleatorio(): Int {
        val coloresSuaves = listOf(
            "#FFCDD2", "#F8BBD0", "#E1BEE7", "#D1C4E9",
            "#C5CAE9", "#BBDEFB", "#B3E5FC", "#B2EBF2",
            "#B2DFDB", "#C8E6C9", "#DCEDC8", "#F0F4C3",
            "#FFECB3", "#FFE0B2", "#FFCCBC", "#D7CCC8"
        )
        return Color.parseColor(coloresSuaves.random())
    }
}
