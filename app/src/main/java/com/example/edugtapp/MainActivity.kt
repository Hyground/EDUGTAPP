package com.example.edugtapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

    private val loginService = LoginService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inicializarVistas()

        btnLogin.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()
            val contrasenia = etContrasenia.text.toString().trim()

            if (usuario.isEmpty() || contrasenia.isEmpty()) {
                mostrarMensaje("Ingrese usuario y contraseña")
                return@setOnClickListener
            }

            loginService.login(
                usuario,
                contrasenia,
                onSuccess = { respuesta ->
                    runOnUiThread {
                        procesarRespuesta(respuesta)
                    }
                },
                onError = { error ->
                    runOnUiThread {
                        manejarError(error)
                    }
                }
            )
        }
    }

    private fun inicializarVistas() {
        etUsuario = findViewById(R.id.etUsuario)
        etContrasenia = findViewById(R.id.etContrasenia)
        btnLogin = findViewById(R.id.btnLogin)
        tvResultado = findViewById(R.id.tvResultado)
    }

    private fun procesarRespuesta(respuesta: String) {
        try {
            val json = JSONObject(respuesta)
            val nombreCompleto = json.getString("nombreCompleto")
            irAMenu(nombreCompleto)
        } catch (e: Exception) {
            Log.e("LoginDebug", "Error al procesar JSON: ${e.message}")
            mostrarMensaje("Error procesando la respuesta del servidor")
        }
    }

    private fun manejarError(error: String) {
        Log.e("LoginError", error)
        if ("401" in error) {
            mostrarMensaje("Usuario o contraseña incorrectos")
        } else {
            mostrarMensaje("Error de conexión: $error")
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        tvResultado.text = mensaje
    }

    private fun irAMenu(nombre: String) {
        val intent = Intent(this, MenuActivity::class.java).apply {
            putExtra("NOMBRE_DOCENTE", nombre)
        }
        startActivity(intent)
        finish()
    }
}
