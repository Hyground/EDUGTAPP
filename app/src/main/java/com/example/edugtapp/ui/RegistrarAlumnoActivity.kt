package com.example.edugtapp.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.edugtapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_alumno)

        val grado = intent.getStringExtra("GRADO_DOCENTE") ?: "-"
        val seccion = intent.getStringExtra("SECCION_DOCENTE") ?: "-"

        tvGrado = findViewById(R.id.tvGrado)
        tvSeccion = findViewById(R.id.tvSeccion)
        tvGrado.text = "Grado: $grado"
        tvSeccion.text = "Sección: $seccion"

        formulario = findViewById(R.id.formulario)
        listaEstudiantes = findViewById(R.id.listaEstudiantes)
        fabAgregar = findViewById(R.id.fabAgregar)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, estudiantes)
        listaEstudiantes.adapter = adapter

        fabAgregar.setOnClickListener {
            mostrarFormulario()
        }

        listaEstudiantes.setOnItemClickListener { _, _, position, _ ->
            val alumno = estudiantes[position]
            mostrarDialogoOpciones(alumno, position, adapter)
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
            (listaEstudiantes.adapter as ArrayAdapter<*>).notifyDataSetChanged()
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

    private fun mostrarDialogoOpciones(nombre: String, index: Int, adapter: ArrayAdapter<String>) {
        AlertDialog.Builder(this)
            .setTitle("Opciones para $nombre")
            .setItems(arrayOf("Actualizar", "Eliminar", "Cancelar")) { dialog, which ->
                when (which) {
                    0 -> {} // Actualizar pendiente
                    1 -> {
                        estudiantes.removeAt(index)
                        adapter.notifyDataSetChanged()
                    }
                }
                dialog.dismiss()
            }
            .show()
    }
}
