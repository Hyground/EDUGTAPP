package com.example.edugtapp.network

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object RegistroService {
    private val client = OkHttpClient()
    private const val apiUrl = "https://eduapi-production.up.railway.app/api/docentes"

    fun registrarDocente(
        nombre: String,
        cui: String,
        correo: String?,
        usuario: String,
        clave: String,
        gradoId: Int,
        seccionId: Int,
        googleToken: String? = null, // ← soporte para login con Google
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("nombreCompleto", nombre)
            put("cui", cui)
            put("nombreUsuario", usuario)
            put("contrasenia", clave)
            put("gradoId", gradoId)
            put("seccionId", seccionId)
            correo?.let { put("correo", it) }
            googleToken?.let { put("googleToken", it) } // ← se agrega solo si existe
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Error de red: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorBody = response.body?.string() ?: "Error desconocido"
                    onError("Error ${response.code}: $errorBody")
                }
            }
        })
    }

    fun verificarNombreUsuario(
        nombreUsuario: String,
        onResult: (disponible: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = Request.Builder()
            .url("https://eduapi-production.up.railway.app/api/docentes/verificar-usuario?nombre=$nombreUsuario")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Error de red: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = response.body?.string()?.trim()
                    val disponible = JSONObject(body).optBoolean("disponible", false)

                    onResult(disponible)
                } else {
                    onError("Error ${response.code}")
                }
            }
        })
    }
}
