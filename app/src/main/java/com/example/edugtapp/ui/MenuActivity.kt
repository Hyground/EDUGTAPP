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
import com.example.edugtapp.model.DocenteInfo
import com.example.edugtapp.network.EstudianteService

class MenuActivity : AppCompatActivity() {

    private lateinit var docenteInfo: DocenteInfo
    private lateinit var tvCantidadEstudiantes: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        docenteInfo = DocenteInfo.fromIntent(intent)

        val tvNombreDocente = findViewById<TextView>(R.id.tvNombreDocente)
        val tvGradoSeccion = findViewById<TextView>(R.id.tvGradoSeccion)
        tvCantidadEstudiantes = findViewById(R.id.tvCantidadEstudiantes)
        val imgAvatar = findViewById<ImageView>(R.id.imgAvatarDocente)

        tvNombreDocente.text = "Docente: ${docenteInfo.nombre}"
        tvGradoSeccion.text = "Grado: ${docenteInfo.grado}   Sección: ${docenteInfo.seccion}"

        imgAvatar.setOnClickListener {
            Toast.makeText(this, "Función para cambiar foto próximamente", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnRegistrarAlumnos).setOnClickListener {
            startActivity(docenteInfo.applyTo(Intent(this, RegistrarAlumnoActivity::class.java)))
        }

        findViewById<Button>(R.id.btnCrearActividades).setOnClickListener {
            startActivity(docenteInfo.applyTo(Intent(this, RegistrarActividadActivity::class.java)))
        }

        findViewById<Button>(R.id.btnRegistrarNotas).setOnClickListener {
            startActivity(docenteInfo.applyTo(Intent(this, RegistrarNotasActivity::class.java)))
        }

        findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            getSharedPreferences("eduPrefs", MODE_PRIVATE).edit().clear().apply()
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        EstudianteService.obtenerEstudiantesPorDocente(docenteInfo.docenteId) { lista ->
            runOnUiThread {
                tvCantidadEstudiantes.text = "Estudiantes: ${lista.size}"
            }
        }
    }
}
