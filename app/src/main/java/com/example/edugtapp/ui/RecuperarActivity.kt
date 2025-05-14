package com.example.edugtapp.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.edugtapp.network.RecuperarService
import com.example.edugtapp.R
import androidx.appcompat.app.AlertDialog



class RecuperarActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var recuperarButton: Button
    private val recuperarService = RecuperarService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar)

        emailEditText = findViewById(R.id.emailEditText)
        recuperarButton = findViewById(R.id.recuperarButton)

        recuperarButton.setOnClickListener {
            val correo = emailEditText.text.toString().trim()

            if (correo.isEmpty()) {
                Toast.makeText(this, "Ingrese su correo Registrado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            recuperarButton.isEnabled = false

            recuperarService.enviarTokenRecuperacion(
                correo,
                onSuccess = {
                    runOnUiThread {
                        AlertDialog.Builder(this@RecuperarActivity)
                            .setTitle("Recuperación enviada")
                            .setMessage("Si el correo está registrado, se ha enviado un enlace de recuperación.")
                            .setCancelable(false)
                            .setPositiveButton("Aceptar") { dialog, _ ->
                                dialog.dismiss()
                                finish()
                            }
                            .show()
                    }
                },
                onError = {
                    runOnUiThread {
                        recuperarButton.isEnabled = true
                        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }
}
