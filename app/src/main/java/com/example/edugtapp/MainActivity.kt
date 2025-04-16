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
            mostrarMensaje("Ingrese usuario y contraseña")
            return
        }

        mostrarCarga(true)
        tvResultado.text = ""

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
            val usuarioID = json.optInt("usuarioID", -1)
            val nombreGrado = json.optJSONObject("grado")?.optString("nombre") ?: ""
            val nombreSeccion = json.optJSONObject("seccion")?.optString("nombre") ?: ""

            val gradoId = json.optJSONObject("grado")?.optInt("id") ?: 0
            val seccionId = json.optJSONObject("seccion")?.optInt("id") ?: 0

            if (nombreCompleto != null && usuarioID != -1) {
                irAMenu(nombreCompleto, nombreGrado, nombreSeccion, usuarioID, gradoId, seccionId)
            } else {
                mostrarMensaje("Respuesta inesperada del servidor")
            }
        } catch (e: Exception) {
            mostrarMensaje("Error procesando la respuesta del servidor")
        }
    }

    private fun irAMenu(nombre: String, grado: String, seccion: String, usuarioID: Int, gradoId: Int, seccionId: Int) {
        startActivity(Intent(this, MenuActivity::class.java).apply {
            putExtra("NOMBRE_DOCENTE", nombre)
            putExtra("GRADO_DOCENTE", grado)
            putExtra("SECCION_DOCENTE", seccion)
            putExtra("DOCENTE_ID", usuarioID)
            putExtra("GRADO_ID", gradoId)
            putExtra("SECCION_ID", seccionId)
        })
        finish()
    }

    private fun mostrarMensaje(mensaje: String) {
        tvResultado.text = mensaje
    }

    private fun mostrarCarga(cargando: Boolean) {
        progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !cargando
    }
}
