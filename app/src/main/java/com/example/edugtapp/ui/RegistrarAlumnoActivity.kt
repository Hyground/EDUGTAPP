package com.example.edugtapp.ui

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.edugtapp.R
import com.example.edugtapp.network.Estudiante
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

    private val estudiantes = mutableListOf<Estudiante>()
    private lateinit var adapter: BaseAdapter
    private var itemExpandido = -1
    private var indexEnEdicion = -1
    private var gradoId: Int = 0
    private var seccionId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_alumno)

        gradoId = intent.getIntExtra("GRADO_ID", 0)
        seccionId = intent.getIntExtra("SECCION_ID", 0)

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

        inicializarAdapter()
        listaEstudiantes.adapter = adapter

        fabAgregar.setOnClickListener { mostrarFormularioParaNuevo() }
        btnGuardar.setOnClickListener { guardarOActualizarEstudiante() }
        btnCancelar.setOnClickListener {
            limpiarCampos()
            formulario.visibility = View.GONE
            fabAgregar.visibility = View.VISIBLE
            indexEnEdicion = -1
        }

        if (docenteId != -1) cargarEstudiantes(docenteId)
    }

    private fun inicializarAdapter() {
        adapter = object : BaseAdapter() {
            override fun getCount() = estudiantes.size
            override fun getItem(position: Int) = estudiantes[position]
            override fun getItemId(position: Int) = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val view = convertView ?: layoutInflater.inflate(R.layout.list_item_estudiante, parent, false)
                val estudiante = estudiantes[position]

                view.findViewById<TextView>(R.id.tvLinea1).text = "${estudiante.nombre} ${estudiante.apellido}"
                view.findViewById<TextView>(R.id.tvLinea2).text = "Código: ${estudiante.codigo}"
                view.findViewById<TextView>(R.id.tvLinea3).text = "CUI No.: ${estudiante.cui}"

                val opcionesLayout = view.findViewById<LinearLayout>(R.id.opcionesLayout)
                val rootItem = view as LinearLayout

                opcionesLayout.visibility = if (position == itemExpandido) View.VISIBLE else View.GONE
                rootItem.setBackgroundResource(
                    if (position == itemExpandido) R.drawable.bg_estudiante_card_selected
                    else R.drawable.bg_estudiante_card
                )

                view.setOnClickListener {
                    itemExpandido = if (itemExpandido == position) -1 else position
                    formulario.visibility = View.GONE
                    fabAgregar.visibility = if (itemExpandido == -1) View.VISIBLE else View.GONE
                    notifyDataSetChanged()
                }

                view.findViewById<Button>(R.id.btnActualizar).setOnClickListener {
                    cargarFormularioDesdeEstudiante(estudiante)
                    indexEnEdicion = position
                    formulario.visibility = View.VISIBLE
                    fabAgregar.visibility = View.GONE
                    itemExpandido = -1
                    notifyDataSetChanged()
                }

                view.findViewById<Button>(R.id.btnEliminar).setOnClickListener {
                    EstudianteService.desactivarEstudiante(estudiante.cui) { exito, mensaje ->
                        runOnUiThread {
                            if (exito) {
                                estudiantes.removeAt(position)
                                Toast.makeText(this@RegistrarAlumnoActivity, "Estudiante retirado", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@RegistrarAlumnoActivity, "Error: $mensaje", Toast.LENGTH_LONG).show()
                            }
                            itemExpandido = -1
                            formulario.visibility = View.GONE
                            fabAgregar.visibility = View.VISIBLE
                            notifyDataSetChanged()
                        }
                    }
                }

                return view
            }
        }
    }

    private fun guardarOActualizarEstudiante() {
        val estudiante = Estudiante(
            nombre = edtNombre.text.toString(),
            apellido = edtApellido.text.toString(),
            codigo = edtCodigo.text.toString(),
            cui = edtCui.text.toString(),
            gradoId = gradoId,
            seccionId = seccionId,
            activo = true
        )

        if (indexEnEdicion != -1) {
            EstudianteService.actualizarEstudiante(estudiante) { exito, mensaje ->
                runOnUiThread {
                    if (exito) {
                        estudiantes[indexEnEdicion] = estudiante
                        Toast.makeText(this, "Actualizado correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error: $mensaje", Toast.LENGTH_LONG).show()
                    }
                    finalizarFormulario()
                }
            }
        } else {
            EstudianteService.crearEstudiante(estudiante) { exito, mensaje ->
                runOnUiThread {
                    if (exito) {
                        estudiantes.add(estudiante)
                        Toast.makeText(this, "Estudiante registrado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error: $mensaje", Toast.LENGTH_LONG).show()
                    }
                    finalizarFormulario()
                }
            }
        }
    }

    private fun cargarFormularioDesdeEstudiante(estudiante: Estudiante) {
        edtNombre.setText(estudiante.nombre)
        edtApellido.setText(estudiante.apellido)
        edtCodigo.setText(estudiante.codigo)
        edtCui.setText(estudiante.cui)
        edtCui.isEnabled = false //  Desactivar el campo CUI al editar
    }

    private fun mostrarFormularioParaNuevo() {
        formulario.visibility = View.VISIBLE
        fabAgregar.visibility = View.GONE
        limpiarCampos()
        edtCui.isEnabled = true //  Habilitar CUI solo al crear nuevo
        indexEnEdicion = -1
    }

    private fun limpiarCampos() {
        edtCui.text.clear()
        edtCodigo.text.clear()
        edtNombre.text.clear()
        edtApellido.text.clear()
    }

    private fun finalizarFormulario() {
        adapter.notifyDataSetChanged()
        formulario.visibility = View.GONE
        fabAgregar.visibility = View.VISIBLE
        indexEnEdicion = -1
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
