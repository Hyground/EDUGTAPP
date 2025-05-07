package com.example.edugtapp.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.edugtapp.R
import com.example.edugtapp.model.DocenteInfo
import com.example.edugtapp.network.ActividadService
import com.example.edugtapp.network.CursoService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.bottomsheet.BottomSheetDialog

class RegistrarActividadActivity : AppCompatActivity() {

    private lateinit var tvSeleccionBimestre: TextView
    private lateinit var tvSeleccionCurso: TextView
    private lateinit var listActividades: ListView
    private lateinit var btnAdd: FloatingActionButton
    private lateinit var tvGradoSeccion: TextView

    private lateinit var formulario: LinearLayout
    private lateinit var edtNombre: EditText
    private lateinit var edtPonderacion: EditText
    private lateinit var rgTipo: RadioGroup
    private lateinit var rbActividad: RadioButton
    private lateinit var rbEvaluacion: RadioButton
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button

    private val bimestres = listOf(
        1 to "I BIMESTRE", 2 to "II BIMESTRE",
        3 to "III BIMESTRE", 4 to "IV BIMESTRE"
    )

    private lateinit var docenteInfo: DocenteInfo
    private var cursoIds = listOf<Int>()
    private var selectedCursoId: Int? = null
    private var selectedBimestreId: Int? = null

    private val actividades = mutableListOf<Map<String, String>>()
    private lateinit var adapter: BaseAdapter
    private var itemExpandido = -1
    private var indexEnEdicion = -1
    private var idActividadEnEdicion: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_actividad)

        docenteInfo = DocenteInfo.fromIntent(intent)
        inicializarVista()
        inicializarAdapter()

        tvGradoSeccion.text = "Grado: ${docenteInfo.grado}                     Sección: ${docenteInfo.seccion}"
        listActividades.adapter = adapter

        cargarCursos()
        btnAdd.setOnClickListener { mostrarFormulario() }
        btnCancelar.setOnClickListener { limpiarFormulario() }
        btnGuardar.setOnClickListener { guardarOActualizarActividad() }

        tvSeleccionBimestre.setOnClickListener { mostrarBimestres() }
        tvSeleccionCurso.setOnClickListener { mostrarCursos() }
    }

    private fun inicializarVista() {
        tvSeleccionBimestre = findViewById(R.id.tvSeleccionBimestre)
        tvSeleccionCurso = findViewById(R.id.tvSeleccionCurso)
        listActividades = findViewById(R.id.listActividades)
        btnAdd = findViewById(R.id.btnAdd)
        tvGradoSeccion = findViewById(R.id.tvGradoSeccion)

        formulario = findViewById(R.id.formularioActividad)
        edtNombre = findViewById(R.id.edtNombre)
        edtPonderacion = findViewById(R.id.edtPonderacion)
        rgTipo = findViewById(R.id.rgTipo)
        rbActividad = findViewById(R.id.rbActividad)
        rbEvaluacion = findViewById(R.id.rbEvaluacion)
        btnGuardar = findViewById(R.id.btnGuardarActividad)
        btnCancelar = findViewById(R.id.btnCancelarActividad)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun inicializarAdapter() {
        adapter = object : BaseAdapter() {
            override fun getCount() = actividades.size
            override fun getItem(position: Int) = actividades[position]
            override fun getItemId(position: Int) = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val view = convertView ?: layoutInflater.inflate(R.layout.list_item_actividad, parent, false)
                val actividad = actividades[position]

                view.findViewById<TextView>(R.id.tvNombre).text = actividad["nombre"]
                view.findViewById<TextView>(R.id.tvTipo).text = "Tipo: ${actividad["tipo"]}"
                view.findViewById<TextView>(R.id.tvPonderacion).text = "Ponderación: ${actividad["ponderacion"]}"

                val opciones = view.findViewById<LinearLayout>(R.id.opcionesLayoutActividad)
                opciones.visibility = if (position == itemExpandido) View.VISIBLE else View.GONE

                val colores = listOf(
                    "#FFCDD2", "#F8BBD0", "#E1BEE7", "#D1C4E9",
                    "#C5CAE9", "#BBDEFB", "#B3E5FC", "#B2EBF2",
                    "#B2DFDB", "#C8E6C9", "#DCEDC8", "#F0F4C3",
                    "#FFECB3", "#FFE0B2", "#FFCCBC"
                )
                val color = Color.parseColor(colores[position % colores.size])
                val fondo = view.background?.mutate() as? GradientDrawable
                fondo?.setColor(color)

                view.setOnClickListener {
                    itemExpandido = if (itemExpandido == position) -1 else position
                    formulario.visibility = View.GONE
                    btnAdd.visibility = if (itemExpandido == -1) View.VISIBLE else View.GONE
                    notifyDataSetChanged()
                }

                view.findViewById<Button>(R.id.btnActualizarActividad).setOnClickListener {
                    cargarFormularioDesdeActividad(actividad)
                    indexEnEdicion = position
                    itemExpandido = -1
                    formulario.visibility = View.VISIBLE
                    btnAdd.hide()
                    notifyDataSetChanged()
                }

                view.findViewById<Button>(R.id.btnEliminarActividad).setOnClickListener {
                    val id = actividad["id"]?.toIntOrNull()
                    if (id != null) {
                        ActividadService.eliminarActividad(id) {
                            runOnUiThread {
                                Toast.makeText(this@RegistrarActividadActivity, "Actividad eliminada", Toast.LENGTH_SHORT).show()
                                if (indexEnEdicion == position || idActividadEnEdicion == id) {
                                    limpiarFormulario()
                                }
                                refrescarActividades()
                            }
                        }
                    } else {
                        Toast.makeText(this@RegistrarActividadActivity, "ID inválido", Toast.LENGTH_SHORT).show()
                    }
                }

                view.findViewById<Button>(R.id.btnCalificarActividad).setOnClickListener {
                    Toast.makeText(this@RegistrarActividadActivity, "Calificar no implementado", Toast.LENGTH_SHORT).show()
                }

                return view
            }
        }
    }

    private fun cargarFormularioDesdeActividad(act: Map<String, String>) {
        edtNombre.setText(act["nombre"])
        edtPonderacion.setText(act["ponderacion"])
        if (act["tipo"] == "Actividad") rbActividad.isChecked = true
        if (act["tipo"] == "Evaluacion") rbEvaluacion.isChecked = true
        idActividadEnEdicion = act["id"]?.toIntOrNull()
    }

    private fun cargarCursos() {
        CursoService.obtenerCursosPorGrado(docenteInfo.gradoId) { jsonArray ->
            runOnUiThread {
                val nombres = mutableListOf<String>()
                val ids = mutableListOf<Int>()
                for (i in 0 until jsonArray.length()) {
                    val curso = jsonArray.getJSONObject(i)
                    nombres.add(curso.getString("nombreCurso"))
                    ids.add(curso.getInt("id"))
                }
                cursoIds = ids
                tvSeleccionCurso.tag = nombres
            }
        }
    }

    private fun mostrarBimestres() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_lista, null)
        val list = view.findViewById<ListView>(R.id.listaOpciones)

        val opciones = bimestres.map { it.second }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, opciones)
        list.adapter = adapter
        list.choiceMode = ListView.CHOICE_MODE_SINGLE

        selectedBimestreId?.let { id ->
            val index = bimestres.indexOfFirst { it.first == id }
            if (index != -1) list.setItemChecked(index, true)
        }

        list.setOnItemClickListener { _, _, position, _ ->
            selectedBimestreId = bimestres[position].first
            tvSeleccionBimestre.text = bimestres[position].second
            dialog.dismiss()
            refrescarActividades()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun mostrarCursos() {
        val nombres = tvSeleccionCurso.tag as? List<String> ?: return
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_lista, null)
        val list = view.findViewById<ListView>(R.id.listaOpciones)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, nombres)
        list.adapter = adapter
        list.choiceMode = ListView.CHOICE_MODE_SINGLE

        selectedCursoId?.let { id ->
            val index = cursoIds.indexOf(id)
            if (index != -1) list.setItemChecked(index, true)
        }

        list.setOnItemClickListener { _, _, position, _ ->
            selectedCursoId = cursoIds.getOrNull(position)
            tvSeleccionCurso.text = nombres[position]
            dialog.dismiss()
            refrescarActividades()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun refrescarActividades() {
        val cursoId = selectedCursoId ?: return
        val bimestreId = selectedBimestreId ?: return
        cargarActividades(docenteInfo.gradoId, docenteInfo.seccionId, cursoId, bimestreId)
    }

    private fun cargarActividades(gradoId: Int, seccionId: Int, cursoId: Int, bimestreId: Int) {
        ActividadService.obtenerActividades(gradoId, seccionId, cursoId, bimestreId) { jsonArray ->
            runOnUiThread {
                actividades.clear()
                for (i in 0 until jsonArray.length()) {
                    val act = jsonArray.getJSONObject(i)
                    actividades.add(
                        mapOf(
                            "id" to act.getInt("id").toString(),
                            "nombre" to act.getString("nombre"),
                            "tipo" to act.getString("tipo"),
                            "ponderacion" to act.getDouble("ponderacion").toString()
                        )
                    )
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun guardarOActualizarActividad() {
        val nombre = edtNombre.text.toString().trim()
        val ponderacion = edtPonderacion.text.toString().toDoubleOrNull() ?: 0.0
        val tipo = when {
            rbActividad.isChecked -> "Actividad"
            rbEvaluacion.isChecked -> "Evaluacion"
            else -> ""
        }

        val cursoId = selectedCursoId ?: return
        val bimestreId = selectedBimestreId ?: return

        if (nombre.isEmpty() || tipo.isEmpty() || ponderacion <= 0.0) {
            Toast.makeText(this, "Complete todos los campos correctamente", Toast.LENGTH_SHORT).show()
            return
        }

        if (indexEnEdicion != -1 && idActividadEnEdicion != null) {
            Log.d("RegistrarActividad", "Actualizando ID $idActividadEnEdicion con nombre=$nombre, tipo=$tipo, ponderación=$ponderacion")
            ActividadService.actualizarActividad(
                idActividadEnEdicion!!,
                docenteInfo.gradoId,
                docenteInfo.seccionId,
                cursoId,
                bimestreId,
                nombre,
                tipo,
                ponderacion
            ) {
                runOnUiThread {
                    Toast.makeText(this, "Actividad actualizada", Toast.LENGTH_SHORT).show()
                    limpiarFormulario()
                    refrescarActividades()
                }
            }
        } else {
            ActividadService.crearActividad(docenteInfo.gradoId, docenteInfo.seccionId, cursoId, bimestreId, nombre, tipo, ponderacion) {
                runOnUiThread {
                    Toast.makeText(this, "Actividad registrada", Toast.LENGTH_SHORT).show()
                    limpiarFormulario()
                    refrescarActividades()
                }
            }
        }
    }

    private fun mostrarFormulario() {
        formulario.visibility = View.VISIBLE
        btnAdd.hide()
        indexEnEdicion = -1
        idActividadEnEdicion = null
    }

    private fun limpiarFormulario() {
        formulario.visibility = View.GONE
        btnAdd.show()
        edtNombre.text.clear()
        edtPonderacion.text.clear()
        rgTipo.clearCheck()
        indexEnEdicion = -1
        idActividadEnEdicion = null
    }
}
