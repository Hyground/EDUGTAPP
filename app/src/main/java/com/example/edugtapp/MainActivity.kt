package com.example.edugtapp

import android.content.Intent
import android.os.Bundle
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

        etUsuario = findViewById(R.id.etUsuario)
        etContrasenia = findViewById(R.id.etContrasenia)
        btnLogin = findViewById(R.id.btnLogin)
        tvResultado = findViewById(R.id.tvResultado)

        btnLogin.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()
            val contrasenia = etContrasenia.text.toString().trim()

            if (usuario.isEmpty() || contrasenia.isEmpty()) {
                tvResultado.text = "Ingrese usuario y contraseña"
                return@setOnClickListener
            }

            loginService.login(
                usuario,
                contrasenia,
                onSuccess = { respuesta ->
                    runOnUiThread {
                        try {
                            val json = JSONObject(respuesta)
                            val nombreCompleto = json.getString("nombreCompleto")
                            irAMenu(nombreCompleto)
                        } catch (e: Exception) {
                            tvResultado.text = "Error procesando respuesta"
                        }
                    }
                },
                onError = { error ->
                    runOnUiThread {
                        tvResultado.text = error
                    }
                }
            )
        }
    }

    private fun irAMenu(nombre: String) {
        val intent = Intent(this, MenuActivity::class.java)
        intent.putExtra("NOMBRE_DOCENTE", nombre)
        startActivity(intent)
        finish()
    }
}
