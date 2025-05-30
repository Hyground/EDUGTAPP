package com.example.edugtapp.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.edugtapp.R
import com.example.edugtapp.network.CursoService
import com.example.edugtapp.network.EstudianteService
import com.example.edugtapp.network.NotaService
import com.example.edugtapp.ui.adapter.ActividadAdapter
import com.example.edugtapp.ui.adapter.BimestreAdapter
import com.example.edugtapp.ui.adapter.CursoAdapter
import org.json.JSONObject

class RegistrarNotaDesdeEstudianteActivity : AppCompatActivity() {

    private lateinit var tvEstudianteNombre: TextView
    private lateinit var tvCursoSeleccionado: TextView
    private lateinit var tvBimestreSeleccionado: TextView
    private lateinit var tvResumenNotas: TextView
    private lateinit var rvCursos: RecyclerView
    private lateinit var rvBimestres: RecyclerView
    private lateinit var rvActividades: RecyclerView

    private lateinit var estudianteCui: String
    private var gradoId: Int = -1
    private var seccionId: Int = -1
    private var cursoSeleccionadoId: Int = -1
    private var nombreCursoSeleccionado: String = ""
    private var nombreBimestreSeleccionado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_nota_estudiante)

        estudianteCui = intent.getStringExtra("CUI_ESTUDIANTE") ?: return

        tvEstudianteNombre = findViewById(R.id.tvEstudianteNombre)
        tvCursoSeleccionado = findViewById(R.id.tvCursoSeleccionado)
        tvBimestreSeleccionado = findViewById(R.id.tvBimestreSeleccionado)
        tvResumenNotas = findViewById(R.id.tvResumenNotas)
        rvCursos = findViewById(R.id.rvCursos)
        rvBimestres = findViewById(R.id.rvBimestres)
        rvActividades = findViewById(R.id.rvActividades)

        rvCursos.layoutManager = GridLayoutManager(this, 2)
        rvBimestres.layoutManager = GridLayoutManager(this, 2)
        rvActividades.layoutManager = GridLayoutManager(this, 2)

        cargarDatosEstudiante(estudianteCui)
    }

    private fun cargarDatosEstudiante(cui: String) {
        EstudianteService.obtenerEstudiantePorCui(cui) { estudiante ->
            runOnUiThread {
                if (estudiante != null) {
                    val nombreCompleto = getString(
                        R.string.nombre_formato,
                        estudiante.nombre,
                        estudiante.apellido
                    ).uppercase()
                    tvEstudianteNombre.text = nombreCompleto
                    gradoId = estudiante.gradoId
                    seccionId = estudiante.seccionId
                    cargarCursosPorGrado(gradoId)
                } else {
                    Toast.makeText(this, "Estudiante no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun cargarCursosPorGrado(gradoId: Int) {
        CursoService.obtenerCursosPorGrado(gradoId) { jsonArray ->
            runOnUiThread {
                val listaCursos = (0 until jsonArray.length()).map {
                    val obj = jsonArray.getJSONObject(it)
                    Pair(obj.getInt("id"), obj.getString("nombreCurso"))
                }

                rvCursos.visibility = View.VISIBLE
                rvBimestres.visibility = View.GONE
                rvActividades.visibility = View.GONE
                tvResumenNotas.visibility = View.GONE

                rvCursos.adapter = CursoAdapter(listaCursos) { cursoId, nombreCurso ->
                    cursoSeleccionadoId = cursoId
                    nombreCursoSeleccionado = nombreCurso
                    tvCursoSeleccionado.text = getString(R.string.curso_formato, nombreCursoSeleccionado)
                    mostrarBimestres()
                }
            }
        }
    }

    private fun mostrarBimestres() {
        val listaBimestres = listOf(
            Pair(1, "I Bimestre"),
            Pair(2, "II Bimestre"),
            Pair(3, "III Bimestre"),
            Pair(4, "IV Bimestre")
        )

        rvCursos.visibility = View.GONE
        rvBimestres.visibility = View.VISIBLE
        rvActividades.visibility = View.GONE
        tvResumenNotas.visibility = View.GONE

        rvBimestres.adapter = BimestreAdapter(listaBimestres) { bimestreId, nombreBimestre ->
            nombreBimestreSeleccionado = nombreBimestre
            tvBimestreSeleccionado.text = getString(R.string.bimestre_formato, nombreBimestreSeleccionado)
            cargarActividades(bimestreId)
        }
    }

    private fun cargarActividades(bimestreId: Int) {
        NotaService.obtenerEvaluacionesConNota(
            gradoId, seccionId, cursoSeleccionadoId, bimestreId, estudianteCui
        ) { evaluacionesArray ->
            runOnUiThread {
                rvBimestres.visibility = View.GONE

                if (evaluacionesArray.length() == 0) {
                    Toast.makeText(this, "No hay evaluaciones registradas", Toast.LENGTH_SHORT).show()
                    rvActividades.visibility = View.GONE
                    tvResumenNotas.visibility = View.GONE
                } else {
                    val listaEvaluaciones = mutableListOf<JSONObject>()
                    for (i in 0 until evaluacionesArray.length()) {
                        listaEvaluaciones.add(evaluacionesArray.getJSONObject(i))
                    }

                    rvActividades.adapter = ActividadAdapter(listaEvaluaciones, estudianteCui) {
                        recalcularResumenNotas(listaEvaluaciones)
                    }

                    rvActividades.visibility = View.VISIBLE
                    tvResumenNotas.visibility = View.VISIBLE
                    recalcularResumenNotas(listaEvaluaciones)
                }
            }
        }
    }

    private fun recalcularResumenNotas(listaEvaluaciones: List<JSONObject>) {
        var totalNota = 0.0
        var totalPonderacion = 0.0

        for (evaluacion in listaEvaluaciones) {
            totalPonderacion += evaluacion.optDouble("ponderacion", 0.0)
            val nota = evaluacion.optDouble("nota", -1.0)
            if (nota >= 0) totalNota += nota
        }

        val resumenTexto = getString(
            R.string.resumen_notas_formato,
            totalNota.toInt(),
            totalPonderacion.toInt()
        )
        tvResumenNotas.text = resumenTexto
    }
}
