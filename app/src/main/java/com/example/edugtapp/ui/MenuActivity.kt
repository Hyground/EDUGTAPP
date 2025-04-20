package com.example.edugtapp.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.edugtapp.MainActivity
import com.example.edugtapp.R
import com.example.edugtapp.model.DocenteInfo
import com.example.edugtapp.network.DocenteService
import com.example.edugtapp.network.EstudianteService

class MenuActivity : AppCompatActivity() {

    private lateinit var docenteInfo: DocenteInfo
    private lateinit var tvCantidadEstudiantes: TextView
    private lateinit var imgAvatar: ImageView
    private lateinit var imgEditIcon: ImageView

    private val requestCambiarFoto = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        docenteInfo = DocenteInfo.fromIntent(intent)

        tvCantidadEstudiantes = findViewById(R.id.tvCantidadEstudiantes)
        imgAvatar = findViewById(R.id.imgAvatarDocente)
        imgEditIcon = findViewById(R.id.imgEditIcon)

        findViewById<TextView>(R.id.tvNombreDocente).text = getString(R.string.text_docente_con_nombre, docenteInfo.nombre)
        findViewById<TextView>(R.id.tvGradoSeccion).text = getString(R.string.text_grado_seccion, docenteInfo.grado, docenteInfo.seccion)

        cargarFotoDocente()

        val cambiarFotoIntent = docenteInfo.applyTo(Intent(this, CambiarFotoActivity::class.java))
        imgAvatar.setOnClickListener { startActivityForResult(cambiarFotoIntent, requestCambiarFoto) }
        imgEditIcon.setOnClickListener { startActivityForResult(cambiarFotoIntent, requestCambiarFoto) }

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
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            })
            finish()
        }
    }

    private fun cargarFotoDocente() {
        DocenteService.obtenerFotoDocente(docenteInfo.docenteId) { url ->
            runOnUiThread {
                Glide.with(this)
                    .load(url ?: R.drawable.ic_user_placeholder)
                    .placeholder(R.drawable.ic_user_placeholder)
                    .into(imgAvatar)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        EstudianteService.obtenerEstudiantesPorDocente(docenteInfo.docenteId) { lista ->
            runOnUiThread {
                tvCantidadEstudiantes.text = getString(R.string.text_estudiantes, lista.size)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCambiarFoto && resultCode == Activity.RESULT_OK) {
            cargarFotoDocente()
        }
    }
}
