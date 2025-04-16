package com.example.edugtapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.edugtapp.R
import com.example.edugtapp.network.EstudianteService
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
    private lateinit var adapter: BaseAdapter
    private var itemExpandido = -1
    private var indexEnEdicion = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_alumno)

        val grado = intent.getStringExtra("GRADO_DOCENTE") ?: "-"
        val seccion = intent.getStringExtra("SECCION_DOCENTE") ?: "-"
        val docenteId = intent.getIntExtra("DOCENTE_ID", -1)

        tvGrado = findViewById(R.id.tvGrado)
        tvSeccion = findViewById(R.id.tvSeccion)
        formulario = findViewById(R.id.formulario)
        listaEstudiantes = findViewById(R.id.listaEstudiantes)
        fabAgregar = findViewById(R.id.fabAgregar)

        edtCui = findViewById(R.id.edtCui)
        edtCodigo = findViewById(R.id.edtCodigo)
        edtNombre = findViewById(R.id.edtNombre)
        edtApellido = findViewById(R.id.edtApellido)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelar = findViewById(R.id.btnCancelar)

        tvGrado.text = "Grado: $grado"
        tvSeccion.text = "Sección: $seccion"

        adapter = object : BaseAdapter() {
            override fun getCount(): Int = estudiantes.size
            override fun getItem(position: Int): Any = estudiantes[position]
            override fun getItemId(position: Int): Long = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val view = convertView ?: LayoutInflater.from(this@RegistrarAlumnoActivity)
                    .inflate(R.layout.list_item_estudiante, parent, false)

                val tvLinea1 = view.findViewById<TextView>(R.id.tvLinea1)
                val tvLinea2 = view.findViewById<TextView>(R.id.tvLinea2)
                val tvLinea3 = view.findViewById<TextView>(R.id.tvLinea3)
                val opcionesLayout = view.findViewById<LinearLayout>(R.id.opcionesLayout)
                val btnActualizar = view.findViewById<Button>(R.id.btnActualizar)
                val btnEliminar = view.findViewById<Button>(R.id.btnEliminar)
                val rootItem = view as LinearLayout

                val datos = estudiantes[position].split("\n")
                tvLinea1.text = datos.getOrNull(0) ?: ""
                tvLinea2.text = datos.getOrNull(1) ?: ""
                tvLinea3.text = datos.getOrNull(2) ?: ""

                opcionesLayout.visibility = if (position == itemExpandido) View.VISIBLE else View.GONE

                // 🔘 Cambiar color de fondo al estar expandido
                if (position == itemExpandido) {
                    rootItem.setBackgroundResource(R.drawable.bg_estudiante_card_selected)
                } else {
                    rootItem.setBackgroundResource(R.drawable.bg_estudiante_card)
                }

                view.setOnClickListener {
                    itemExpandido = if (itemExpandido == position) -1 else position
                    formulario.visibility = View.GONE
                    fabAgregar.visibility = if (itemExpandido == -1) View.VISIBLE else View.GONE
                    notifyDataSetChanged()
                }

                btnActualizar.setOnClickListener {
                    cargarFormularioDesdeTexto(estudiantes[position])
                    indexEnEdicion = position
                    formulario.visibility = View.VISIBLE
                    fabAgregar.visibility = View.GONE
                    itemExpandido = -1
                    notifyDataSetChanged()
                }

                btnEliminar.setOnClickListener {
                    estudiantes.removeAt(position)
                    itemExpandido = -1
                    formulario.visibility = View.GONE
                    fabAgregar.visibility = View.VISIBLE
                    notifyDataSetChanged()
                }

                return view
            }
        }

        listaEstudiantes.adapter = adapter

        fabAgregar.setOnClickListener {
            mostrarFormularioParaNuevo()
        }

        btnGuardar.setOnClickListener {
            val nuevoTexto = "${edtNombre.text} ${edtApellido.text}\nCódigo: ${edtCodigo.text}\nCUI No.: ${edtCui.text}"
            if (indexEnEdicion != -1) {
                estudiantes[indexEnEdicion] = nuevoTexto
            } else {
                estudiantes.add(nuevoTexto)
            }
            adapter.notifyDataSetChanged()
            formulario.visibility = View.GONE
            fabAgregar.visibility = View.VISIBLE
            indexEnEdicion = -1
        }

        btnCancelar.setOnClickListener {
            limpiarCampos()
            formulario.visibility = View.GONE
            fabAgregar.visibility = View.VISIBLE
            indexEnEdicion = -1
        }

        if (docenteId != -1) {
            cargarEstudiantes(docenteId)
        } else {
            Toast.makeText(this, "ID de docente inválido", Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarFormularioParaNuevo() {
        formulario.visibility = View.VISIBLE
        fabAgregar.visibility = View.GONE
        limpiarCampos()
        indexEnEdicion = -1
    }

    private fun cargarFormularioDesdeTexto(texto: String) {
        val lineas = texto.split("\n")
        val nombreCompleto = lineas.getOrNull(0)?.split(" ") ?: listOf("", "")
        val codigo = lineas.getOrNull(1)?.removePrefix("Código: ") ?: ""
        val cui = lineas.getOrNull(2)?.removePrefix("CUI No.: ") ?: ""

        edtNombre.setText(nombreCompleto.firstOrNull() ?: "")
        edtApellido.setText(nombreCompleto.drop(1).joinToString(" "))
        edtCodigo.setText(codigo)
        edtCui.setText(cui)
    }

    private fun limpiarCampos() {
        edtCui.text.clear()
        edtCodigo.text.clear()
        edtNombre.text.clear()
        edtApellido.text.clear()
    }

    private fun cargarEstudiantes(docenteId: Int) {
        EstudianteService.obtenerEstudiantesPorDocente(docenteId) { lista ->
            runOnUiThread {
                estudiantes.clear()
                estudiantes.addAll(lista)
                adapter.notifyDataSetChanged()
            }
        }
    }
}
