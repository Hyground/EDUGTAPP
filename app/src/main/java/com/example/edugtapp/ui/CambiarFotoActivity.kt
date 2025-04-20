package com.example.edugtapp.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.edugtapp.R
import com.example.edugtapp.model.DocenteInfo
import com.example.edugtapp.network.DocenteService
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CambiarFotoActivity : AppCompatActivity() {

    private lateinit var docenteInfo: DocenteInfo
    private lateinit var imageView: ImageView
    private lateinit var currentPhotoPath: String
    private var selectedImageFile: File? = null

    private val REQUEST_IMAGE_PICK = 1001
    private val REQUEST_IMAGE_CAPTURE = 1002
    private val REQUEST_PERMISSIONS = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cambiar_foto)

        docenteInfo = DocenteInfo.fromIntent(intent)
        imageView = findViewById(R.id.imagePreview)

        if (!permisosConcedidos()) {
            solicitarPermisos()
        }

        findViewById<Button>(R.id.btnSeleccionarGaleria).setOnClickListener {
            if (permisosConcedidos()) {
                val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickIntent, REQUEST_IMAGE_PICK)
            } else {
                solicitarPermisos()
            }
        }

        findViewById<Button>(R.id.btnTomarFoto).setOnClickListener {
            if (permisosConcedidos()) {
                try {
                    val photoFile = createImageFile()
                    val photoURI = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    selectedImageFile = photoFile
                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al abrir la cámara", Toast.LENGTH_SHORT).show()
                }
            } else {
                solicitarPermisos()
            }
        }

        findViewById<Button>(R.id.btnSubirFoto).setOnClickListener {
            selectedImageFile?.let {
                DocenteService.subirFotoDocente(docenteInfo.docenteId, it) { exito ->
                    runOnUiThread {
                        val msg = if (exito) "Foto actualizada correctamente" else "Error al subir la foto"
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                        if (exito) {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }
                }
            } ?: Toast.makeText(this, "Selecciona o toma una foto primero", Toast.LENGTH_SHORT).show()
        }
    }

    private fun permisosConcedidos(): Boolean {
        val camara = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val lectura = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        return camara == PackageManager.PERMISSION_GRANTED && lectura == PackageManager.PERMISSION_GRANTED
    }

    private fun solicitarPermisos() {
        val permisos = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisos.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permisos.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        ActivityCompat.requestPermissions(this, permisos.toTypedArray(), REQUEST_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS && !permisosConcedidos()) {
            Toast.makeText(this, "Debes conceder permisos para continuar", Toast.LENGTH_LONG).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir("Pictures")
        return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir!!).also {
            currentPhotoPath = it.absolutePath
        }
    }

    private fun compressImage(uri: Uri): File {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val compressedFile = createImageFile()
        FileOutputStream(compressedFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos)
        }
        return compressedFile
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    data?.data?.let {
                        imageView.setImageURI(it)
                        selectedImageFile = compressImage(it)
                    }
                }
                REQUEST_IMAGE_CAPTURE -> {
                    selectedImageFile?.let {
                        imageView.setImageURI(Uri.fromFile(it))
                    }
                }
            }
        }
    }
}
