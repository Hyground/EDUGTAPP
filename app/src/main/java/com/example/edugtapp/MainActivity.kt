package com.example.edugtapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.edugtapp.network.LoginService
import com.example.edugtapp.ui.MenuActivity
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var etUsuario: EditText
    private lateinit var etContrasenia: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvResultado: TextView
    private lateinit var progressBar: ProgressBar
    private val loginService = LoginService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inicializarVistas()
        btnLogin.setOnClickListener { iniciarSesion() }
    }

    private fun inicializarVistas() {
        etUsuario = findViewById(R.id.etUsuario)
        etContrasenia = findViewById(R.id.etContrasenia)
        btnLogin = findViewById(R.id.btnLogin)
        tvResultado = findViewById(R.id.tvResultado)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun iniciarSesion() {
        val usuario = etUsuario.text.toString().trim()
        val contrasenia = etContrasenia.text.toString().trim()
        if (usuario.isEmpty() || contrasenia.isEmpty()) {
            tvResultado.text = "Ingrese usuario y contraseña"
            return
        }

        mostrarCarga(true)
        loginService.login(
            usuario, contrasenia,
            onSuccess = { procesarRespuesta(it) },
            onError = { mostrarError(it) }
        )
    }

    private fun procesarRespuesta(respuesta: String) {
        runOnUiThread {
            mostrarCarga(false)
            try {
                val json = JSONObject(respuesta)
                val nombreCompleto = json.optString("nombreCompleto")
                val usuarioID = json.optInt("usuarioID", -1)
                val grado = json.optJSONObject("grado")
                val seccion = json.optJSONObject("seccion")
                if (nombreCompleto != null && usuarioID != -1) {
                    startActivity(Intent(this, MenuActivity::class.java).apply {
                        putExtra("NOMBRE_DOCENTE", nombreCompleto)
                        putExtra("DOCENTE_ID", usuarioID)
                        putExtra("GRADO_DOCENTE", grado?.optString("nombre") ?: "")
                        putExtra("SECCION_DOCENTE", seccion?.optString("nombre") ?: "")
                        putExtra("GRADO_ID", grado?.optInt("id") ?: 0)
                        putExtra("SECCION_ID", seccion?.optInt("id") ?: 0)
                    })
                    finish()
                } else {
                    tvResultado.text = "Respuesta inesperada del servidor"
                }
            } catch (_: Exception) {
                tvResultado.text = "Error procesando la respuesta"
            }
        }
    }

    private fun mostrarError(error: String) {
        runOnUiThread {
            mostrarCarga(false)
            tvResultado.text = if ("401" in error) {
                "Usuario o contraseña incorrectos"
            } else {
                error
            }
        }
    }

    private fun mostrarCarga(mostrar: Boolean) {
        progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !mostrar
    }
}
