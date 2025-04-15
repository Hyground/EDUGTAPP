package com.example.edugtapp.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.edugtapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class RegistrarAlumnoActivity : AppCompatActivity() {

    private lateinit var tvGrado: TextView
    private lateinit var tvSeccion: TextView
    private lateinit var listaEstudiantes: ListView
    private lateinit var formulario: View
    private lateinit var fabAgregar: FloatingActionButton

    private lateinit var edtCui: EditText
    private lateinit var edtCodigo: EditText
    private lateinit var edtNombre: EditText
    private lateinit var edtApellido: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button

    private val estudiantes = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_alumno)

        val grado = intent.getStringExtra("GRADO_DOCENTE") ?: "-"
        val seccion = intent.getStringExtra("SECCION_DOCENTE") ?: "-"
        val docenteId = intent.getIntExtra("DOCENTE_ID", -1)

        tvGrado = findViewById(R.id.tvGrado)
        tvSeccion = findViewById(R.id.tvSeccion)
        tvGrado.text = "Grado: $grado"
        tvSeccion.text = "Sección: $seccion"

        formulario = findViewById(R.id.formulario)
        listaEstudiantes = findViewById(R.id.listaEstudiantes)
        fabAgregar = findViewById(R.id.fabAgregar)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, estudiantes)
        listaEstudiantes.adapter = adapter

        fabAgregar.setOnClickListener {
            mostrarFormulario()
        }

        listaEstudiantes.setOnItemClickListener { _, _, position, _ ->
            val alumno = estudiantes[position]
            mostrarDialogoOpciones(alumno, position)
        }

        if (docenteId != -1) {
            obtenerEstudiantes(docenteId)
        } else {
            Toast.makeText(this, "ID de docente inválido", Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarFormulario() {
        formulario.visibility = View.VISIBLE

        edtCui = findViewById(R.id.edtCui)
        edtCodigo = findViewById(R.id.edtCodigo)
        edtNombre = findViewById(R.id.edtNombre)
        edtApellido = findViewById(R.id.edtApellido)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelar = findViewById(R.id.btnCancelar)

        btnGuardar.setOnClickListener {
            val nuevo = "${edtNombre.text} ${edtApellido.text}"
            estudiantes.add(nuevo)
            adapter.notifyDataSetChanged()
            limpiarCampos()
            formulario.visibility = View.GONE
        }

        btnCancelar.setOnClickListener {
            limpiarCampos()
            formulario.visibility = View.GONE
        }
    }

    private fun limpiarCampos() {
        edtCui.text.clear()
        edtCodigo.text.clear()
        edtNombre.text.clear()
        edtApellido.text.clear()
    }

    private fun mostrarDialogoOpciones(nombre: String, index: Int) {
        AlertDialog.Builder(this)
            .setTitle("Opciones para $nombre")
            .setItems(arrayOf("Actualizar", "Eliminar", "Cancelar")) { dialog, which ->
                when (which) {
                    0 -> Toast.makeText(this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
                    1 -> {
                        estudiantes.removeAt(index)
                        adapter.notifyDataSetChanged()
                    }
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun obtenerEstudiantes(docenteId: Int) {
        val url = "https://eduapi-production.up.railway.app/api/estudiantes/por-docente/$docenteId"
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RegistrarAlumnoActivity, "Error al conectar con la API", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("RegistrarAlumno", "Respuesta JSON: $responseBody")
                    responseBody?.let { json ->
                        try {
                            val jsonArray = JSONArray(json)
                            estudiantes.clear()
                            for (i in 0 until jsonArray.length()) {
                                val estudiante = jsonArray.getJSONObject(i)
                                val nombre = estudiante.optString("nombre") + " " + estudiante.optString("apellido")
                                estudiantes.add(nombre)
                            }
                            runOnUiThread {
                                Log.d("RegistrarAlumno", "Total estudiantes: ${estudiantes.size}")
                                adapter.notifyDataSetChanged()
                                Toast.makeText(this@RegistrarAlumnoActivity, "${estudiantes.size} estudiantes cargados", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                Toast.makeText(this@RegistrarAlumnoActivity, "Error al procesar estudiantes", Toast.LENGTH_SHORT).show()
                            }
                            Log.e("RegistrarAlumno", "Error procesando JSON", e)
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@RegistrarAlumnoActivity, "No se pudo obtener estudiantes", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
