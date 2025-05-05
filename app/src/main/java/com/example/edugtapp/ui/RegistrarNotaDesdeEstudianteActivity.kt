package com.example.edugtapp.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.edugtapp.R
import com.example.edugtapp.network.ActividadService
import com.example.edugtapp.network.CursoService
import com.example.edugtapp.network.EstudianteService
import com.example.edugtapp.ui.adapter.BimestreAdapter
import com.example.edugtapp.ui.adapter.CursoAdapter

class RegistrarNotaDesdeEstudianteActivity : AppCompatActivity() {

    private lateinit var tvEstudianteNombre: TextView
    private lateinit var tvCursoSeleccionado: TextView
    private lateinit var tvBimestreSeleccionado: TextView
    private lateinit var rvCursos: RecyclerView
    private lateinit var rvBimestres: RecyclerView

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
        rvCursos = findViewById(R.id.rvCursos)
        rvBimestres = findViewById(R.id.rvBimestres)

        rvCursos.layoutManager = GridLayoutManager(this, 2)
        rvBimestres.layoutManager = GridLayoutManager(this, 2)

        cargarDatosEstudiante(estudianteCui)
    }

    private fun cargarDatosEstudiante(cui: String) {
        EstudianteService.obtenerEstudiantePorCui(cui) { estudiante ->
            runOnUiThread {
                if (estudiante != null) {
                    val nombreCompleto = "${estudiante.nombre} ${estudiante.apellido}".uppercase()
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

                rvCursos.adapter = CursoAdapter(listaCursos) { cursoId, nombreCurso ->
                    cursoSeleccionadoId = cursoId
                    nombreCursoSeleccionado = nombreCurso
                    tvCursoSeleccionado.text = "Curso: $nombreCursoSeleccionado"
                    mostrarBimestres()
                }
            }
        }
    }

    private fun mostrarBimestres() {
        val listaBimestres = listOf(
            Pair(1, "Bimestre 1"),
            Pair(2, "Bimestre 2"),
            Pair(3, "Bimestre 3"),
            Pair(4, "Bimestre 4")
        )

        rvCursos.visibility = View.GONE
        rvBimestres.visibility = View.VISIBLE

        rvBimestres.adapter = BimestreAdapter(listaBimestres) { bimestreId, nombreBimestre ->
            nombreBimestreSeleccionado = nombreBimestre
            tvBimestreSeleccionado.text = "Bimestre: $nombreBimestreSeleccionado"
            cargarActividades(bimestreId)
        }
    }

    private fun cargarActividades(bimestreId: Int) {
        ActividadService.obtenerActividades(
            gradoId,
            seccionId,
            cursoSeleccionadoId,
            bimestreId
        ) { actividadesArray ->
            runOnUiThread {
                if (actividadesArray.length() == 0) {
                    Toast.makeText(this, "No hay actividades registradas", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this,
                        "${actividadesArray.length()} actividades encontradas",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Aquí podrías abrir otra pantalla con las actividades
                }
            }
        }
    }
}
