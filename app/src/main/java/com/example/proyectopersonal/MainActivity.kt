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

// Actividad principal para registrar componentes con foto y ubicación, consultar historial y abrir el catálogo
class MainActivity : AppCompatActivity() {

    // --- CONTRATOS Y VARIABLES PARA CÁMARA/GALERÍA/GPS (FUERA DE onCreate) ---
    // Ruta de la foto seleccionada y archivo temporal de cámara en curso
    private var fotoPath: String? = null
    private var archivoFotoActual: File? = null
    // Vistas para mostrar la imagen elegida y las coordenadas GPS
    private lateinit var ivFoto: ImageView
    private lateinit var tvUbicacion: TextView

    // Cliente de ubicación de Google Play Services y coordenadas capturadas
    private lateinit var clienteUbicacion: FusedLocationProviderClient
    private var latitud: Double? = null
    private var longitud: Double? = null

    // Solicita el permiso de cámara y abre la cámara si el usuario lo acepta
    private val pedirPermisoCamara = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) lanzarCamara()
        else Toast.makeText(this, "Sin permiso no puedo hacer la foto", Toast.LENGTH_SHORT).show()
    }

    // Recibe el resultado de capturar una foto, la guarda en preferencias o borra el archivo si falló
    private val hacerFoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito: Boolean ->
        val archivo = archivoFotoActual
        if (exito && archivo != null) {
            fotoPath = archivo.absolutePath
            ivFoto.setImageBitmap(Fotos.cargar(fotoPath))
            getSharedPreferences("MisDatos", MODE_PRIVATE).edit().putString("FOTO_PERFIL", fotoPath).apply()
        } else {
            archivo?.delete()
        }
    }

    // Copia la imagen seleccionada de la galería al almacenamiento interno de la app
    private val elegirDeGaleria = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val ruta = Fotos.copiarDesdeUri(this, uri)
            if (ruta != null) {
                fotoPath = ruta
                ivFoto.setImageBitmap(Fotos.cargar(ruta))
                getSharedPreferences("MisDatos", MODE_PRIVATE).edit().putString("FOTO_PERFIL", ruta).apply()
            }
        }
    }

    // Solicita permisos de ubicación precisa o aproximada y consulta el GPS si se conceden
    private val pedirPermisosUbicacion = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        val fina = resultado[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val aproximada = resultado[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fina || aproximada) obtenerUbicacion()
        else Toast.makeText(this, "Sin permiso no puedo saber dónde estás", Toast.LENGTH_SHORT).show()
    }
    // -----------------------------------------------------------------------

    // Inicializa vistas, recupera datos guardados y configura validaciones y navegación
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtiene los campos del formulario y los botones principales
        val editNombre = findViewById<EditText>(R.id.editNombre)
        val editPrecio = findViewById<EditText>(R.id.editPrecio)
        val spinnerCategoria = findViewById<Spinner>(R.id.spinnerCategoria)
        val boton = findViewById<Button>(R.id.botonAgregar)
        val botonVerHistorial = findViewById<Button>(R.id.botonVerHistorial)
        val botonCatalogoOnline = findViewById<Button>(R.id.botonCatalogoOnline)

        // --- ENLACES DE CÁMARA, GALERÍA Y GPS (DENTRO DE onCreate) ---
        ivFoto = findViewById(R.id.ivFoto)
        tvUbicacion = findViewById(R.id.tvUbicacion)
        val botonFoto = findViewById<Button>(R.id.btnFoto)
        val botonGaleria = findViewById<Button>(R.id.btnGaleria)
        val botonUbicacion = findViewById<Button>(R.id.btnUbicacion)

        // Inicializa el proveedor de ubicación
        try {
            clienteUbicacion = LocationServices.getFusedLocationProviderClient(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Configura los botones de cámara, selector de galería y ubicación
        botonFoto.setOnClickListener { pulsarBotonFoto() }
        botonGaleria.setOnClickListener {
            elegirDeGaleria.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
        botonUbicacion.setOnClickListener { pulsarBotonUbicacion() }
        // -------------------------------------------------------------

        // Obtiene el DAO y restaura los últimos valores guardados en SharedPreferences
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

        val fotoGuardada = prefs.getString("FOTO_PERFIL", null)
        if (fotoGuardada != null) {
            fotoPath = fotoGuardada
            ivFoto.setImageBitmap(Fotos.cargar(fotoGuardada))
        }

        // Valida los datos, guarda en preferencias e inserta el componente en Room
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

                    // Inserta el componente en segundo plano y abre SecondActivity con los datos
                    lifecycleScope.launch {
                        try {
                            dao.insertar(nuevoComponente)

                            latitud = null
                            longitud = null

                            val intent = Intent(this@MainActivity, SecondActivity::class.java)
                            intent.putExtra("NOMBRE_EXTRA", nombre)
                            intent.putExtra("PRECIO_EXTRA", precio)
                            intent.putExtra("CATEGORIA_EXTRA", categoria)
                            intent.putExtra("FOTO_EXTRA", fotoPath)
                            startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this@MainActivity, "Error al guardar componente", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        // Navega a la pantalla de historial de componentes
        botonVerHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }

        // Navega a la pantalla del catálogo online
        botonCatalogoOnline.setOnClickListener {
            startActivity(Intent(this, CatalogoActivity::class.java))
        }
    }

    // Actualiza el resumen de cantidad y precio total cada vez que la pantalla vuelve a verse
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

    // Comprueba el permiso de cámara antes de lanzarla
    private fun pulsarBotonFoto() {
        val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permiso == PackageManager.PERMISSION_GRANTED) lanzarCamara()
        else pedirPermisoCamara.launch(Manifest.permission.CAMERA)
    }

    // Crea un archivo destino y lanza la aplicación de cámara
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

    // Verifica si ya se dispone de permiso de ubicación fina o aproximada
    private fun tienePermisoUbicacion(): Boolean {
        val fina = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val aprox = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fina == PackageManager.PERMISSION_GRANTED || aprox == PackageManager.PERMISSION_GRANTED
    }

    // Obtiene la ubicación si hay permiso o solicita los permisos necesarios
    private fun pulsarBotonUbicacion() {
        if (tienePermisoUbicacion()) obtenerUbicacion()
        else pedirPermisosUbicacion.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    // Solicita la ubicación actual con alta precisión y muestra las coordenadas en pantalla
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