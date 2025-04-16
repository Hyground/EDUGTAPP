package com.example.edugtapp.network

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LoginService {
    private val client = OkHttpClient()
    private val apiUrl = "https://eduapi-production.up.railway.app/api/docentes/login"

    fun login(
        usuario: String,
        contrasenia: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("nombreUsuario", usuario)
            put("contrasenia", contrasenia)
        }

        val requestBody = json.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = onError("Error de red: ${e.message}")
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) onSuccess(body)
                else onError("Código: ${response.code}, $body")
            }
        })
    }
}
