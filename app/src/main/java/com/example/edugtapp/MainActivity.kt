package com.example.edugtapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.edugtapp.network.LoginService
import com.example.edugtapp.ui.MenuActivity
import com.example.edugtapp.ui.RecuperarActivity
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var rememberCheckBox: CheckBox
    private lateinit var forgotPasswordText: TextView

    private lateinit var sharedPrefs: SharedPreferences
    private val loginService = LoginService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPrefs = getSharedPreferences("eduPrefs", MODE_PRIVATE)

        inicializarVistas()
        cargarUsuarioGuardado()

        loginButton.setOnClickListener { iniciarSesion() }

        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, RecuperarActivity::class.java))
        }

    }

    private fun inicializarVistas() {
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        rememberCheckBox = findViewById(R.id.rememberCheckBox)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)
    }

    private fun cargarUsuarioGuardado() {
        val guardado = sharedPrefs.getBoolean("RECORDAR", false)
        if (guardado) {
            usernameEditText.setText(sharedPrefs.getString("USUARIO", ""))
            passwordEditText.setText(sharedPrefs.getString("CLAVE", ""))
            rememberCheckBox.isChecked = true
        }
    }

    private fun iniciarSesion() {
        val usuario = usernameEditText.text.toString().trim()
        val contrasenia = passwordEditText.text.toString().trim()

        if (usuario.isEmpty() || contrasenia.isEmpty()) {
            Toast.makeText(this, "Ingrese usuario y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        loginButton.isEnabled = false

        loginService.login(
            usuario, contrasenia,
            onSuccess = { procesarRespuesta(it, usuario, contrasenia) },
            onError = { mostrarError(it) }
        )
    }

    private fun procesarRespuesta(respuesta: String, usuario: String, clave: String) {
        runOnUiThread {
            loginButton.isEnabled = true
            try {
                val json = JSONObject(respuesta)
                val nombreCompleto = json.optString("nombreCompleto")
                val usuarioID = json.optInt("usuarioID", -1)
                val grado = json.optJSONObject("grado")
                val seccion = json.optJSONObject("seccion")

                if (usuarioID != -1) {
                    if (rememberCheckBox.isChecked) {
                        sharedPrefs.edit()
                            .putBoolean("RECORDAR", true)
                            .putString("USUARIO", usuario)
                            .putString("CLAVE", clave)
                            .apply()
                    } else {
                        sharedPrefs.edit().clear().apply()
                    }

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
                    Toast.makeText(this, "Respuesta inválida del servidor", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                Toast.makeText(this, "Error procesando la respuesta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarError(error: String) {
        runOnUiThread {
            loginButton.isEnabled = true
            val mensaje = if ("401" in error) {
                "Usuario o contraseña incorrectos"
            } else {
                error
            }
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }
    }
}
