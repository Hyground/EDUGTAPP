package com.example.edugtapp.model

import android.content.Intent

data class DocenteInfo(
    val nombre: String,
    val grado: String,
    val seccion: String,
    val docenteId: Int,
    val gradoId: Int,
    val seccionId: Int
) {
    fun applyTo(intent: Intent): Intent {
        return intent.apply {
            putExtra("NOMBRE_DOCENTE", nombre)
            putExtra("GRADO_DOCENTE", grado)
            putExtra("SECCION_DOCENTE", seccion)
            putExtra("DOCENTE_ID", docenteId)
            putExtra("GRADO_ID", gradoId)
            putExtra("SECCION_ID", seccionId)
        }
    }

    companion object {
        fun fromIntent(intent: Intent): DocenteInfo {
            return DocenteInfo(
                nombre = intent.getStringExtra("NOMBRE_DOCENTE") ?: "-",
                grado = intent.getStringExtra("GRADO_DOCENTE") ?: "-",
                seccion = intent.getStringExtra("SECCION_DOCENTE") ?: "-",
                docenteId = intent.getIntExtra("DOCENTE_ID", -1),
                gradoId = intent.getIntExtra("GRADO_ID", 0),
                seccionId = intent.getIntExtra("SECCION_ID", 0)
            )
        }
    }
}
