package com.example.edugtapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.edugtapp.MainActivity
import com.example.edugtapp.R
import com.example.edugtapp.network.EstudianteService

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val nombreDocente = intent.getStringExtra("NOMBRE_DOCENTE") ?: "-"
        val gradoDocente = intent.getStringExtra("GRADO_DOCENTE") ?: "-"
        val seccionDocente = intent.getStringExtra("SECCION_DOCENTE") ?: "-"
        val docenteId = intent.getIntExtra("DOCENTE_ID", -1)
        val gradoId = intent.getIntExtra("GRADO_ID", 0)
        val seccionId = intent.getIntExtra("SECCION_ID", 0)

        val tvNombreDocente = findViewById<TextView>(R.id.tvNombreDocente)
        val tvGradoSeccion = findViewById<TextView>(R.id.tvGradoSeccion)
        val tvCantidadEstudiantes = findViewById<TextView>(R.id.tvCantidadEstudiantes)
        val imgAvatar = findViewById<ImageView>(R.id.imgAvatarDocente)

        tvNombreDocente.text = "Docente: $nombreDocente"
        tvGradoSeccion.text = "Grado: $gradoDocente   Sección: $seccionDocente"

        // Contar estudiantes según docente
        EstudianteService.obtenerEstudiantesPorDocente(docenteId) { lista ->
            runOnUiThread {
                val cantidad = lista.size
                tvCantidadEstudiantes.text = "Estudiantes: $cantidad"
            }
        }

        imgAvatar.setOnClickListener {
            Toast.makeText(this, "Función para cambiar foto próximamente", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnRegistrarAlumnos).setOnClickListener {
            startActivity(Intent(this, RegistrarAlumnoActivity::class.java).apply {
                putExtra("NOMBRE_DOCENTE", nombreDocente)
                putExtra("GRADO_DOCENTE", gradoDocente)
                putExtra("SECCION_DOCENTE", seccionDocente)
                putExtra("DOCENTE_ID", docenteId)
                putExtra("GRADO_ID", gradoId)
                putExtra("SECCION_ID", seccionId)
            })
        }

        findViewById<Button>(R.id.btnCrearActividades).setOnClickListener {
            startActivity(Intent(this, RegistrarActividadActivity::class.java).apply {
                putExtra("NOMBRE_DOCENTE", nombreDocente)
                putExtra("GRADO_DOCENTE", gradoDocente)
                putExtra("SECCION_DOCENTE", seccionDocente)
                putExtra("DOCENTE_ID", docenteId)
                putExtra("GRADO_ID", gradoId)
                putExtra("SECCION_ID", seccionId)
            })
        }

        findViewById<Button>(R.id.btnRegistrarNotas).setOnClickListener {
            // TODO: Implementar registro de notas
        }

        findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            val prefs = getSharedPreferences("eduPrefs", MODE_PRIVATE)
            prefs.edit().clear().apply()

            val intent = Intent(this@MenuActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
