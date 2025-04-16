package com.example.edugtapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.edugtapp.R
import com.example.edugtapp.MainActivity

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

        findViewById<TextView>(R.id.tvBienvenida).text = "Bienvenido, $nombreDocente"

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
            // TODO: Implementar creación de actividades
        }

        findViewById<Button>(R.id.btnRegistrarNotas).setOnClickListener {
            // TODO: Implementar registro de notas
        }

        // 🔐 Botón cerrar sesión con limpieza de datos
        findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            // Limpiar preferencias (si se usaron)
            val prefs = getSharedPreferences("eduPrefs", MODE_PRIVATE)
            prefs.edit().clear().apply()

            // Volver al login
            val intent = Intent(this@MenuActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
