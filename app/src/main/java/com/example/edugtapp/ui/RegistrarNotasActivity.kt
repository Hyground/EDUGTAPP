package com.example.edugtapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.edugtapp.R
import com.example.edugtapp.model.DocenteInfo

class RegistrarNotasActivity : AppCompatActivity() {

    private lateinit var docenteInfo: DocenteInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_registrar_notas)

        docenteInfo = DocenteInfo.fromIntent(intent)

        // TODO: Implementar pantalla de registro de notas usando:
        // docenteInfo.grado, docenteInfo.seccion, docenteInfo.docenteId, etc.
    }
}
