package com.example.edugtapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.edugtapp.network.LoginService
import com.example.edugtapp.ui.MenuActivity
import org.json.JSONObject
import com.example.edugtapp.ui.RecuperarActivity
class MainActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var rememberCheckBox: CheckBox
    private lateinit var forgotPasswordText: TextView
    private lateinit var loginErrorText: TextView
    private lateinit var progressBar: ProgressBar

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
        findViewById<TextView>(R.id.registerText).setOnClickListener {
            startActivity(Intent(this, com.example.edugtapp.ui.RegistrarDocenteActivity::class.java))
        }

    }

    private fun inicializarVistas() {
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        rememberCheckBox = findViewById(R.id.rememberCheckBox)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)
        loginErrorText = findViewById(R.id.loginErrorText)
        progressBar = findViewById(R.id.progressBar)
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
            loginErrorText.text = "Ingrese usuario y contraseña"
            loginErrorText.visibility = View.VISIBLE
            return
        }

        loginButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
        loginErrorText.visibility = View.GONE

        loginService.login(
            usuario, contrasenia,
            onSuccess = { procesarRespuesta(it, usuario, contrasenia) },
            onError = { mostrarError(it) }
        )
    }

    private fun procesarRespuesta(respuesta: String, usuario: String, clave: String) {
        runOnUiThread {
            loginButton.isEnabled = true
            progressBar.visibility = View.GONE

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
                    loginErrorText.text = "Respuesta inválida del servidor"
                    loginErrorText.visibility = View.VISIBLE
                }
            } catch (_: Exception) {
                loginErrorText.text = "Error procesando la respuesta"
                loginErrorText.visibility = View.VISIBLE
            }
        }
    }

    private fun mostrarError(error: String) {
        runOnUiThread {
            loginButton.isEnabled = true
            progressBar.visibility = View.GONE

            loginErrorText.text = if ("401" in error) {
                "Usuario o contraseña incorrectos"
            } else {
                error
            }
            loginErrorText.visibility = View.VISIBLE
        }
    }
}
