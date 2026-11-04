package com.example.proyectopersonal

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    // --- CONTRATOS Y VARIABLES PARA CÁMARA/GALERÍA/GPS (FUERA DE onCreate) ---
    private var fotoPath: String? = null
    private var archivoFotoActual: File? = null
    private lateinit var ivFoto: ImageView
    private lateinit var tvUbicacion: TextView

    private lateinit var clienteUbicacion: FusedLocationProviderClient
    private var latitud: Double? = null
    private var longitud: Double? = null

    private val pedirPermisoCamara = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) lanzarCamara()
        else Toast.makeText(this, "Sin permiso no puedo hacer la foto", Toast.LENGTH_SHORT).show()
    }

    private val hacerFoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito: Boolean ->
        val archivo = archivoFotoActual
        if (exito && archivo != null) {
            fotoPath = archivo.absolutePath
            ivFoto.setImageBitmap(Fotos.cargar(fotoPath))
        } else {
            archivo?.delete()
        }
    }

    private val elegirDeGaleria = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val ruta = Fotos.copiarDesdeUri(this, uri)
            if (ruta != null) {
                fotoPath = ruta
                ivFoto.setImageBitmap(Fotos.cargar(ruta))
            }
        }
    }

    private val pedirPermisosUbicacion = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        val fina = resultado[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val aproximada = resultado[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fina || aproximada) obtenerUbicacion()
        else Toast.makeText(this, "Sin permiso no puedo saber dónde estás", Toast.LENGTH_SHORT).show()
    }
    // -----------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editNombre = findViewById<EditText>(R.id.editNombre)
        val editPrecio = findViewById<EditText>(R.id.editPrecio)
        val spinnerCategoria = findViewById<Spinner>(R.id.spinnerCategoria)
        val boton = findViewById<Button>(R.id.botonAgregar)
        val botonVerHistorial = findViewById<Button>(R.id.botonVerHistorial)

        // --- ENLACES DE CÁMARA, GALERÍA Y GPS (DENTRO DE onCreate) ---
        ivFoto = findViewById(R.id.ivFoto)
        tvUbicacion = findViewById(R.id.tvUbicacion)
        val botonFoto = findViewById<Button>(R.id.btnFoto)
        val botonGaleria = findViewById<Button>(R.id.btnGaleria)
        val botonUbicacion = findViewById<Button>(R.id.btnUbicacion)

        try {
            clienteUbicacion = LocationServices.getFusedLocationProviderClient(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        botonFoto.setOnClickListener { pulsarBotonFoto() }
        botonGaleria.setOnClickListener {
            elegirDeGaleria.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
        botonUbicacion.setOnClickListener { pulsarBotonUbicacion() }
        // -------------------------------------------------------------

        val dao = AppDatabase.obtener(this).componenteDao()

        val prefs = getSharedPreferences("MisDatos", MODE_PRIVATE)
        val nombreGuardado = prefs.getString("NOMBRE_GUARDADO", "")
        editNombre.setText(nombreGuardado)

        val precioGuardado = prefs.getString("PRECIO_GUARDADO", "")
        editPrecio.setText(precioGuardado)

        val categoriaGuardada = prefs.getInt("CATEGORIA_GUARDADA", 0)
        if (categoriaGuardada >= 0 && categoriaGuardada < spinnerCategoria.count) {
            spinnerCategoria.setSelection(categoriaGuardada)
        }

        boton.setOnClickListener {
            val nombre = editNombre.text.toString().trim()
            val precioTextoOriginal = editPrecio.text.toString().trim()
            val precioTexto = precioTextoOriginal.replace(',', '.')
            val categoria = spinnerCategoria.selectedItem?.toString() ?: "Otro"

            val precioNumerico = precioTexto.toDoubleOrNull()

            when {
                nombre.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.error_nombre_vacio), Toast.LENGTH_SHORT).show()
                }
                precioTexto.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.error_precio_vacio), Toast.LENGTH_SHORT).show()
                }
                precioNumerico == null || precioNumerico <= 0.0 -> {
                    Toast.makeText(this, getString(R.string.error_precio_invalido), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    prefs.edit()
                        .putString("NOMBRE_GUARDADO", nombre)
                        .putString("PRECIO_GUARDADO", precioTextoOriginal)
                        .putInt("CATEGORIA_GUARDADA", spinnerCategoria.selectedItemPosition)
                        .apply()

                    val precio = precioNumerico

                    val nuevoComponente = Componente(
                        nombre = nombre,
                        precio = precio,
                        categoria = categoria,
                        latitud = latitud,
                        longitud = longitud,
                        fotoPath = fotoPath
                    )

                    lifecycleScope.launch {
                        try {
                            dao.insertar(nuevoComponente)

                            fotoPath = null
                            latitud = null
                            longitud = null
                            ivFoto.setImageDrawable(null)

                            val intent = Intent(this@MainActivity, SecondActivity::class.java)
                            intent.putExtra("NOMBRE_EXTRA", nombre)
                            intent.putExtra("PRECIO_EXTRA", precio)
                            intent.putExtra("CATEGORIA_EXTRA", categoria)
                            startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this@MainActivity, "Error al guardar componente", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        botonVerHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val textoResumen = findViewById<TextView>(R.id.textoResumen)
        lifecycleScope.launch {
            try {
                val dao = AppDatabase.obtener(this@MainActivity).componenteDao()
                val lista = dao.obtenerTodos()
                val cantidad = lista.size
                val total = lista.sumOf { it.precio }
                val totalFormateado = String.format("%.2f", total)
                textoResumen.text = "Componentes: $cantidad | Total: $totalFormateado €"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun pulsarBotonFoto() {
        val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permiso == PackageManager.PERMISSION_GRANTED) lanzarCamara()
        else pedirPermisoCamara.launch(Manifest.permission.CAMERA)
    }

    private fun lanzarCamara() {
        try {
            val archivo = Fotos.crearArchivo(this)
            archivoFotoActual = archivo
            hacerFoto.launch(Fotos.uriDe(this, archivo))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al abrir la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    private fun tienePermisoUbicacion(): Boolean {
        val fina = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val aprox = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fina == PackageManager.PERMISSION_GRANTED || aprox == PackageManager.PERMISSION_GRANTED
    }

    private fun pulsarBotonUbicacion() {
        if (tienePermisoUbicacion()) obtenerUbicacion()
        else pedirPermisosUbicacion.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion() {
        if (!::clienteUbicacion.isInitialized) {
            Toast.makeText(this, "Servicio de ubicación no disponible", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            clienteUbicacion
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { ubicacion: Location? ->
                    if (ubicacion != null) {
                        latitud = ubicacion.latitude
                        longitud = ubicacion.longitude
                        tvUbicacion.text = "Ubicación: " + "%.4f".format(ubicacion.latitude) + "," + "%.4f".format(ubicacion.longitude)
                    } else {
                        Toast.makeText(this, "No se ha podido obtener la ubicación", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "No se ha podido obtener la ubicación", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}