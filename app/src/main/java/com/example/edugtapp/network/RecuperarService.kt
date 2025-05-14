package com.example.edugtapp.network

import okhttp3.*
import java.io.IOException

class RecuperarService {
    private val client = OkHttpClient()
    private val apiUrl = "https://eduapi-production.up.railway.app/api/cuenta-docente/enviar-token"

    fun enviarTokenRecuperacion(
        correo: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val formBody = FormBody.Builder()
            .add("correo", correo)
            .build()

        val request = Request.Builder()
            .url(apiUrl)
            .post(formBody)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Error de red: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) onSuccess()
                else onError("Error: ${response.code}")
            }
        })
    }
}
