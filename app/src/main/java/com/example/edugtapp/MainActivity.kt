package com.example.edugtapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
            mostrarMensaje("Ingrese usuario y contraseña")
            return
        }

        mostrarCarga(true)
        tvResultado.text = "" // Limpia mensaje anterior

        loginService.login(
            usuario,
            contrasenia,
            onSuccess = { respuesta ->
                runOnUiThread {
                    mostrarCarga(false)
                    procesarRespuesta(respuesta)
                }
            },
            onError = { error ->
                runOnUiThread {
                    mostrarCarga(false)
                    mostrarMensaje(
                        if ("401" in error) "Usuario o contraseña incorrectos. Intente nuevamente."
                        else "Error de conexión: $error"
                    )
                }
            }
        )
    }

    private fun procesarRespuesta(respuesta: String) {
        try {
            val json = JSONObject(respuesta)
            val nombreCompleto = json.optString("nombreCompleto", null)

            if (nombreCompleto != null) {
                irAMenu(nombreCompleto)
            } else {
                mostrarMensaje("Respuesta inesperada del servidor")
            }
        } catch (e: Exception) {
            Log.e("LoginDebug", "Error al procesar JSON: ${e.message}")
            mostrarMensaje("Error procesando la respuesta del servidor")
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        tvResultado.text = mensaje
    }

    private fun mostrarCarga(cargando: Boolean) {
        progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !cargando
    }

    private fun irAMenu(nombre: String) {
        startActivity(Intent(this, MenuActivity::class.java).apply {
            putExtra("NOMBRE_DOCENTE", nombre)
        })
        finish()
    }
}
