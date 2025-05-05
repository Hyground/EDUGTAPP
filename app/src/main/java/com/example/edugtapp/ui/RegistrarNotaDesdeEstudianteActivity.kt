package com.example.edugtapp.ui

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.edugtapp.R
import com.example.edugtapp.network.CursoService
import com.example.edugtapp.network.EstudianteService
import com.example.edugtapp.ui.adapter.CursoAdapter

class RegistrarNotaDesdeEstudianteActivity : AppCompatActivity() {

    private lateinit var tvEstudianteNombre: TextView
    private lateinit var rvCursos: RecyclerView
    private lateinit var estudianteCui: String
    private var gradoId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_nota_estudiante)

        estudianteCui = intent.getStringExtra("CUI_ESTUDIANTE") ?: return

        tvEstudianteNombre = findViewById(R.id.tvEstudianteNombre)
        rvCursos = findViewById(R.id.rvCursos)
        rvCursos.layoutManager = GridLayoutManager(this, 2)

        cargarDatosEstudiante(estudianteCui)
    }

    private fun cargarDatosEstudiante(cui: String) {
        EstudianteService.obtenerEstudiantePorCui(cui) { estudiante ->
            runOnUiThread {
                if (estudiante != null) {
                    tvEstudianteNombre.text = "${estudiante.nombre} ${estudiante.apellido}"
                    gradoId = estudiante.gradoId
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
                val lista = (0 until jsonArray.length()).map {
                    val obj = jsonArray.getJSONObject(it)
                    obj.getString("nombreCurso")
                }
                rvCursos.adapter = CursoAdapter(lista)
            }
        }
    }
}
