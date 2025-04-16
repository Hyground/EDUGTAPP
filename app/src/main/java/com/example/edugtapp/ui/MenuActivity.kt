package com.example.edugtapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.edugtapp.R

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val tvBienvenida = findViewById<TextView>(R.id.tvBienvenida)
        val nombreDocente = intent.getStringExtra("NOMBRE_DOCENTE") ?: "-"
        val gradoDocente = intent.getStringExtra("GRADO_DOCENTE") ?: "-"
        val seccionDocente = intent.getStringExtra("SECCION_DOCENTE") ?: "-"
        val gradoId = intent.getIntExtra("GRADO_ID", 0)
        val seccionId = intent.getIntExtra("SECCION_ID", 0)
        val docenteId = intent.getIntExtra("DOCENTE_ID", -1)

        tvBienvenida.text = "Bienvenido, $nombreDocente"

        val btnRegistrarAlumnos = findViewById<Button>(R.id.btnRegistrarAlumnos)
        val btnCrearActividades = findViewById<Button>(R.id.btnCrearActividades)
        val btnRegistrarNotas = findViewById<Button>(R.id.btnRegistrarNotas)

        btnRegistrarAlumnos.setOnClickListener {
            val intent = Intent(this, RegistrarAlumnoActivity::class.java).apply {
                putExtra("NOMBRE_DOCENTE", nombreDocente)
                putExtra("GRADO_DOCENTE", gradoDocente)
                putExtra("SECCION_DOCENTE", seccionDocente)
                putExtra("DOCENTE_ID", docenteId)
                putExtra("GRADO_ID", gradoId)
                putExtra("SECCION_ID", seccionId)
            }
            startActivity(intent)
        }

        btnCrearActividades.setOnClickListener {
            // TODO: lanzar actividad de creación de actividades
        }

        btnRegistrarNotas.setOnClickListener {
            // TODO: lanzar actividad de registro de notas
        }
    }
}
