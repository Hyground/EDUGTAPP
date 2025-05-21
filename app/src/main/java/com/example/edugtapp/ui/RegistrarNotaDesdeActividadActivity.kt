package com.example.edugtapp.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.edugtapp.R
import com.example.edugtapp.model.DocenteInfo
import com.example.edugtapp.network.Estudiante
import com.example.edugtapp.network.EstudianteService
import com.example.edugtapp.network.NotaService
import org.json.JSONArray
import org.json.JSONObject

class RegistrarNotaDesdeActividadActivity : AppCompatActivity() {

    private lateinit var tvNombreActividad: TextView
    private lateinit var tvPonderacion: TextView
    private lateinit var tvCurso: TextView
    private lateinit var tvBimestre: TextView
    private lateinit var listEstudiantes: ListView
    private lateinit var btnGuardarNotas: Button

    private var idActividad: Int = -1
    private var gradoId: Int = -1
    private var seccionId: Int = -1
    private var nombreActividad: String = ""
    private lateinit var docenteInfo: DocenteInfo

    private val estudiantes = mutableListOf<Estudiante>()
    private val calificaciones = mutableMapOf<String, Double>()
    private val notasExistentes = mutableMapOf<String, Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_nota_actividad)

        tvNombreActividad = findViewById(R.id.tvNombreActividad)
        tvPonderacion = findViewById(R.id.tvPonderacion)
        tvCurso = findViewById(R.id.tvCurso)
        tvBimestre = findViewById(R.id.tvBimestre)
        listEstudiantes = findViewById(R.id.listEstudiantesActividad)
        btnGuardarNotas = findViewById(R.id.btnGuardarNotasActividad)

        // Recibir datos
        idActividad = intent.getIntExtra("idActividad", -1)
        gradoId = intent.getIntExtra("gradoId", -1)
        seccionId = intent.getIntExtra("seccionId", -1)
        nombreActividad = intent.getStringExtra("nombreActividad") ?: ""
        docenteInfo = DocenteInfo.fromIntent(intent)

        val ponderacion = intent.getDoubleExtra("ponderacionActividad", 0.0)
        val nombreCurso = intent.getStringExtra("nombreCurso") ?: ""
        val nombreBimestre = intent.getStringExtra("nombreBimestre") ?: ""

        if (idActividad == -1 || gradoId == -1 || seccionId == -1) {
            Toast.makeText(this, "Datos incompletos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvNombreActividad.text = "Actividad: $nombreActividad"
        tvPonderacion.text = "Ponderación: ${ponderacion.toInt()}"
        tvCurso.text = "Curso: $nombreCurso"
        tvBimestre.text = "Bimestre: $nombreBimestre"

        cargarEstudiantesYNotas()

        btnGuardarNotas.setOnClickListener {
            guardarNotas(ponderacion)
        }
    }

    private fun cargarEstudiantesYNotas() {
        EstudianteService.obtenerEstudiantesPorDocente(docenteInfo.docenteId) { lista ->
            NotaService.obtenerNotasDeActividad(idActividad) { notasArray ->
                runOnUiThread {
                    // Cargar notas existentes
                    for (i in 0 until notasArray.length()) {
                        val obj = notasArray.getJSONObject(i)
                        val estudianteObj = obj.optJSONObject("estudiante")
                        val cui = estudianteObj?.optString("cui") ?: ""
                        val nota = obj.optDouble("nota", -1.0)
                        if (cui.isNotEmpty() && nota >= 0) {
                            notasExistentes[cui] = nota
                        }
                    }

                    val filtrados = lista.filter {
                        it.gradoId == gradoId && it.seccionId == seccionId && it.activo
                    }

                    estudiantes.clear()
                    estudiantes.addAll(filtrados)

                    listEstudiantes.adapter = object : BaseAdapter() {
                        override fun getCount() = estudiantes.size
                        override fun getItem(position: Int) = estudiantes[position]
                        override fun getItemId(position: Int) = position.toLong()

                        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                            val view: View
                            val holder: ViewHolder

                            if (convertView == null) {
                                view = layoutInflater.inflate(R.layout.item_estudiante_calificacion, parent, false)
                                holder = ViewHolder(
                                    tvNombreEstudiante = view.findViewById(R.id.tvNombreEstudiante),
                                    edtNotaEstudiante = view.findViewById(R.id.edtNotaEstudiante)
                                )
                                view.tag = holder
                            } else {
                                view = convertView
                                holder = view.tag as ViewHolder
                            }

                            val estudiante = estudiantes[position]
                            val cui = estudiante.cui

                            holder.tvNombreEstudiante.text = "${estudiante.nombre} ${estudiante.apellido}"

                            // Limpiar listeners antiguos
                            holder.edtNotaEstudiante.setOnFocusChangeListener(null)

                            // Mostrar nota existente
                            val notaExistente = calificaciones[cui] ?: notasExistentes[cui]
                            holder.edtNotaEstudiante.setText(notaExistente?.toString() ?: "")

                            // Listener de foco
                            holder.edtNotaEstudiante.setOnFocusChangeListener { view, hasFocus ->
                                if (hasFocus) {
                                    (view as EditText).post {
                                        view.selectAll()
                                        view.requestRectangleOnScreen(
                                            android.graphics.Rect(0, 0, view.width, view.height), true
                                        )
                                    }
                                } else {
                                    val nota = holder.edtNotaEstudiante.text.toString().toDoubleOrNull()
                                    if (nota != null) {
                                        calificaciones[cui] = nota
                                    } else {
                                        calificaciones.remove(cui)
                                    }
                                }
                            }

                            return view
                        }
                    }
                }
            }
        }
    }

    private fun guardarNotas(ponderacionMaxima: Double) {
        currentFocus?.clearFocus()

        if (calificaciones.isEmpty()) {
            Toast.makeText(this, "No se ingresaron notas", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonArray = JSONArray()
        for ((cui, nota) in calificaciones) {
            if (nota > ponderacionMaxima) {
                Toast.makeText(this, "La nota de $cui excede la ponderación máxima ($ponderacionMaxima)", Toast.LENGTH_LONG).show()
                return
            }
            val json = JSONObject().apply {
                put("cui", cui)
                put("actividadId", idActividad)
                put("nota", nota)
            }
            jsonArray.put(json)
        }

        NotaService.enviarNotasEnLote(jsonArray) { exito ->
            runOnUiThread {
                if (exito) {
                    Toast.makeText(this, "Notas guardadas correctamente", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al guardar notas", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    data class ViewHolder(
        val tvNombreEstudiante: TextView,
        val edtNotaEstudiante: EditText
    )
}
