package com.example.edugtapp.ui

import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.edugtapp.R
import com.example.edugtapp.network.CatalogoService
import com.example.edugtapp.network.CatalogoService.OpcionCatalogo
import com.example.edugtapp.network.RegistroService

class RegistrarDocenteActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etCui: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etUsuario: EditText
    private lateinit var etClave: EditText
    private lateinit var spinnerGrado: Spinner
    private lateinit var spinnerSeccion: Spinner
    private lateinit var btnRegistrar: Button
    private lateinit var btnGoogle: ImageButton
    private lateinit var btnFacebook: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView

    private var gradoSeleccionado: OpcionCatalogo? = null
    private var seccionSeleccionada: OpcionCatalogo? = null
    private var usuarioDisponible: Boolean = false
    private var ultimaConsulta: String = ""

    private val debounceHandler = Handler()
    private var debounceRunnable: Runnable? = null
    private val delayMs = 900L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_docente)

        etNombre = findViewById(R.id.etNombre)
        etCui = findViewById(R.id.etCui)
        etCorreo = findViewById(R.id.etCorreo)
        etUsuario = findViewById(R.id.etUsuario)
        etClave = findViewById(R.id.etClave)
        spinnerGrado = findViewById(R.id.spinnerGrado)
        spinnerSeccion = findViewById(R.id.spinnerSeccion)
        btnRegistrar = findViewById(R.id.btnRegistrarDocente)
        btnGoogle = findViewById(R.id.btnGoogle)
        btnFacebook = findViewById(R.id.btnFacebook)
        progressBar = findViewById(R.id.progressBarRegistro)
        tvError = findViewById(R.id.tvErrorRegistro)

        cargarGrados()
        cargarSecciones()

        etUsuario.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val usuario = s.toString().trim()
                debounceRunnable?.let { debounceHandler.removeCallbacks(it) }

                debounceRunnable = Runnable {
                    if (usuario.isNotEmpty() && usuario != ultimaConsulta) {
                        ultimaConsulta = usuario
                        RegistroService.verificarNombreUsuario(
                            nombreUsuario = usuario,
                            onResult = { disponible ->
                                usuarioDisponible = disponible
                                runOnUiThread {
                                    actualizarIconoUsuario(disponible)
                                }
                            },
                            onError = {
                                usuarioDisponible = false
                                runOnUiThread {
                                    actualizarIconoUsuario(null)
                                }
                            }
                        )
                    } else {
                        usuarioDisponible = false
                        actualizarIconoUsuario(null)
                    }
                }

                debounceHandler.postDelayed(debounceRunnable!!, delayMs)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val cui = etCui.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val usuario = etUsuario.text.toString().trim()
            val clave = etClave.text.toString().trim()
            gradoSeleccionado = spinnerGrado.selectedItem as? OpcionCatalogo
            seccionSeleccionada = spinnerSeccion.selectedItem as? OpcionCatalogo

            if (nombre.isEmpty() || cui.isEmpty() || usuario.isEmpty() || clave.isEmpty()
                || gradoSeleccionado == null || seccionSeleccionada == null) {
                tvError.text = "Todos los campos obligatorios deben estar llenos"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (gradoSeleccionado!!.id == 0 || seccionSeleccionada!!.id == 0) {
                tvError.text = "Debes seleccionar un grado y una sección válidos"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (!usuarioDisponible) {
                tvError.text = "El nombre de usuario no está disponible"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnRegistrar.isEnabled = false
            tvError.visibility = View.GONE

            RegistroService.registrarDocente(
                nombre = nombre,
                cui = cui,
                correo = if (correo.isEmpty()) null else correo,
                usuario = usuario,
                clave = clave,
                gradoId = gradoSeleccionado!!.id,
                seccionId = seccionSeleccionada!!.id,
                onSuccess = {
                    runOnUiThread {
                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                },
                onError = { error ->
                    runOnUiThread {
                        tvError.text = error
                        tvError.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        btnRegistrar.isEnabled = true
                    }
                }
            )
        }

        btnGoogle.setOnClickListener {
            Toast.makeText(this, "Registro con Google en desarrollo", Toast.LENGTH_SHORT).show()
        }

        btnFacebook.setOnClickListener {
            Toast.makeText(this, "Registro con Facebook en desarrollo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarIconoUsuario(disponible: Boolean?) {
        val icono: Drawable? = when (disponible) {
            true -> ContextCompat.getDrawable(this, R.drawable.ic_check)
            false -> ContextCompat.getDrawable(this, R.drawable.ic_error)
            null -> null
        }
        etUsuario.setCompoundDrawablesWithIntrinsicBounds(null, null, icono, null)
    }

    private fun cargarGrados() {
        CatalogoService.obtenerGrados(
            onSuccess = { grados ->
                runOnUiThread {
                    val opcionesConDefault = mutableListOf(OpcionCatalogo(0, "Grado"))
                    opcionesConDefault.addAll(grados)
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcionesConDefault)
                    spinnerGrado.adapter = adapter
                    spinnerGrado.setSelection(0)
                }
            },
            onError = { mostrarError(it) }
        )
    }

    private fun cargarSecciones() {
        CatalogoService.obtenerSecciones(
            onSuccess = { secciones ->
                runOnUiThread {
                    val opcionesConDefault = mutableListOf(OpcionCatalogo(0, "Sección"))
                    opcionesConDefault.addAll(secciones)
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcionesConDefault)
                    spinnerSeccion.adapter = adapter
                    spinnerSeccion.setSelection(0)
                }
            },
            onError = { mostrarError(it) }
        )
    }

    private fun mostrarError(msg: String) {
        runOnUiThread {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
