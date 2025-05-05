package com.example.edugtapp.ui

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.edugtapp.R
import com.example.edugtapp.network.EstudianteService

class RegistrarNotaDesdeEstudianteActivity : AppCompatActivity() {

    private lateinit var tvEstudianteNombre: TextView
    private lateinit var estudianteCui: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_nota_estudiante)

        estudianteCui = intent.getStringExtra("CUI_ESTUDIANTE") ?: return

        tvEstudianteNombre = findViewById(R.id.tvEstudianteNombre)

        cargarDatosEstudiante(estudianteCui)
    }

    private fun cargarDatosEstudiante(cui: String) {
        EstudianteService.obtenerEstudiantePorCui(cui) { estudiante ->
            runOnUiThread {
                if (estudiante != null) {
                    tvEstudianteNombre.text = "${estudiante.nombre} ${estudiante.apellido}"
                } else {
                    Toast.makeText(this, "Estudiante no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
