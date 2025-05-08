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

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    // Si decides volver a usar estos, agrégalos al XML
    private lateinit var tvResultado: TextView
    private lateinit var progressBar: ProgressBar

    private val loginService = LoginService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inicializarVistas()
        loginButton.setOnClickListener { iniciarSesion() }
    }

    private fun inicializarVistas() {
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)

        // Solo descomenta si agregas estos elementos al XML
        // tvResultado = findViewById(R.id.tvResultado)
        // progressBar = findViewById(R.id.progressBar)
    }

    private fun iniciarSesion() {
        val usuario = usernameEditText.text.toString().trim()
        val contrasenia = passwordEditText.text.toString().trim()
        if (usuario.isEmpty() || contrasenia.isEmpty()) {
            Toast.makeText(this, "Ingrese usuario y contraseña", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Respuesta inesperada del servidor", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                Toast.makeText(this, "Error procesando la respuesta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarError(error: String) {
        runOnUiThread {
            mostrarCarga(false)
            val mensaje = if ("401" in error) {
                "Usuario o contraseña incorrectos"
            } else {
                error
            }
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarCarga(mostrar: Boolean) {
        // Si no estás usando progressBar, puedes comentar estas líneas
        // progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
        loginButton.isEnabled = !mostrar
    }
}