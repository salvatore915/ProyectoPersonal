package com.example.proyectopersonal

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.proyectopersonal.network.RetrofitClient
import com.example.proyectopersonal.repository.CatalogoRepository
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

// Actividad que muestra el detalle completo de un producto y permite guardarlo en componentes locales
class DetalleProductoActivity : AppCompatActivity() {

    // Referencias a los elementos visuales de la pantalla de detalle
    private lateinit var ivFoto: ImageView
    private lateinit var tvTitulo: TextView
    private lateinit var tvMarca: TextView
    private lateinit var tvCategoria: TextView
    private lateinit var tvPrecio: TextView
    private lateinit var tvStock: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var btnAgregar: Button
    private lateinit var tvConfirmacion: TextView

    // Inicializa el repositorio de forma perezosa con la base de datos y el cliente de red
    private val repository by lazy {
        val db = AppDatabase.obtener(this)
        CatalogoRepository(
            api = RetrofitClient.api,
            productoDao = db.productoOnlineDao(),
            componenteDao = db.componenteDao()
        )
    }

    // Guarda el producto cargado actualmente para añadirlo a la base de datos local
    private var productoActual: ProductoOnline? = null

    // Configura la interfaz, recupera el ID recibido y prepara las acciones de los botones
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_producto)

        // Obtiene los botones y vistas de la pantalla de detalle
        val btnVolver = findViewById<Button>(R.id.btnVolverDetalle)
        ivFoto = findViewById(R.id.ivDetalleProducto)
        tvTitulo = findViewById(R.id.tvDetalleTitulo)
        tvMarca = findViewById(R.id.tvDetalleMarca)
        tvCategoria = findViewById(R.id.tvDetalleCategoria)
        tvPrecio = findViewById(R.id.tvDetallePrecio)
        tvStock = findViewById(R.id.tvDetalleStock)
        tvRating = findViewById(R.id.tvDetalleRating)
        tvDescripcion = findViewById(R.id.tvDetalleDescripcion)
        btnAgregar = findViewById(R.id.btnAgregarAlPresupuesto)
        tvConfirmacion = findViewById(R.id.tvConfirmacionGuardado)

        // Cierra la actividad y vuelve a la pantalla anterior
        btnVolver.setOnClickListener { finish() }

        // Recupera el ID del producto enviado desde la pantalla anterior y solicita su detalle
        val productoId = intent.getIntExtra("PRODUCTO_ID", -1)
        if (productoId != -1) {
            cargarDetalle(productoId)
        } else {
            Toast.makeText(this, "ID de producto no válido", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Descarga la imagen si existe y guarda el producto como componente local en Room
        btnAgregar.setOnClickListener {
            val producto = productoActual ?: return@setOnClickListener
            btnAgregar.isEnabled = false
            lifecycleScope.launch {
                val ruta = descargarFoto(producto.thumbnail)
                val exito = repository.agregarAComponentesLocales(producto, ruta)
                if (exito) {
                    tvConfirmacion.visibility = View.VISIBLE
                    tvConfirmacion.text = "✓ ¡Añadido a tus componentes locales de TORRECOMS!"
                    Toast.makeText(this@DetalleProductoActivity, "Componente guardado en tu base de datos local", Toast.LENGTH_SHORT).show()
                } else {
                    btnAgregar.isEnabled = true
                    Toast.makeText(this@DetalleProductoActivity, "Error al guardar localmente", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Traduce automáticamente y en tiempo real cualquier texto del inglés al español
    private fun traducirTextoEnSegundoPlano(textoIngles: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Codifica el texto y realiza la petición HTTP al servicio de traducción
                val encoded = URLEncoder.encode(textoIngles, "UTF-8")
                val url = URL("https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&dt=t&q=$encoded")
                val con = url.openConnection() as HttpURLConnection
                con.setRequestProperty("User-Agent", "Mozilla/5.0")
                con.connectTimeout = 4000
                con.readTimeout = 4000
                if (con.responseCode == 200) {
                    // Extrae y concatena los fragmentos traducidos desde la respuesta JSON
                    val respuesta = con.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JsonParser.parseString(respuesta).asJsonArray
                    val partes = jsonArray.get(0).asJsonArray
                    val sb = StringBuilder()
                    for (elemento in partes) {
                        sb.append(elemento.asJsonArray.get(0).asString)
                    }
                    val textoTraducido = sb.toString()
                    // Actualiza la descripción en el hilo principal si la traducción no está vacía
                    if (textoTraducido.isNotBlank()) {
                        withContext(Dispatchers.Main) {
                            tvDescripcion.text = textoTraducido
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Descarga la imagen desde la URL a un archivo local y devuelve su ruta absoluta
    private suspend fun descargarFoto(url: String?): String? = withContext(Dispatchers.IO) {
        if (url.isNullOrBlank()) return@withContext null
        val archivo = Fotos.crearArchivo(this@DetalleProductoActivity)
        try {
            java.net.URL(url).openStream().use { entrada ->
                archivo.outputStream().use { salida -> entrada.copyTo(salida) }
            }
            archivo.absolutePath
        } catch (e: Exception) {
            // Elimina el archivo incompleto si ocurre algún error durante la descarga
            archivo.delete()
            null
        }
    }

    // Consulta los datos del producto por ID en el repositorio y actualiza la vista
    private fun cargarDetalle(id: Int) {
        lifecycleScope.launch {
            val producto = repository.obtenerDetalle(id)
            if (producto != null) {
                productoActual = producto
                mostrarProducto(producto)
            } else {
                Toast.makeText(this@DetalleProductoActivity, "No se pudo cargar la información del producto", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Rellena los campos visuales con la información del producto y dispara la traducción
    private fun mostrarProducto(p: ProductoOnline) {
        tvTitulo.text = p.title
        tvMarca.text = p.brand ?: "Marca oficial"
        tvCategoria.text = "Categoría: ${p.category}"
        tvPrecio.text = "${String.format(Locale.US, "%.2f", p.price)} €"
        tvStock.text = if (p.stock > 0) "En stock (${p.stock} unidades)" else "Agotado"
        tvRating.text = "★ Puntuación: ${String.format(Locale.US, "%.1f", p.rating)} / 5.0"
        tvDescripcion.text = p.description

        // Ejecuta la traducción en segundo plano
        traducirTextoEnSegundoPlano(p.description)

        // Carga la imagen principal del producto con Glide
        Glide.with(this)
            .load(p.thumbnail)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .fitCenter()
            .into(ivFoto)
    }
}