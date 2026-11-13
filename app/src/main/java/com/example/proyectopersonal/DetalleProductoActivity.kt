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
import kotlinx.coroutines.launch
import java.util.Locale

class DetalleProductoActivity : AppCompatActivity() {

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

    private val repository by lazy {
        val db = AppDatabase.obtener(this)
        CatalogoRepository(
            api = RetrofitClient.api,
            productoDao = db.productoOnlineDao(),
            componenteDao = db.componenteDao()
        )
    }

    private var productoActual: ProductoOnline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_producto)

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

        btnVolver.setOnClickListener { finish() }

        val productoId = intent.getIntExtra("PRODUCTO_ID", -1)
        if (productoId != -1) {
            cargarDetalle(productoId)
        } else {
            Toast.makeText(this, "ID de producto no válido", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnAgregar.setOnClickListener {
            val producto = productoActual
            if (producto != null) {
                lifecycleScope.launch {
                    val exito = repository.agregarAComponentesLocales(producto)
                    if (exito) {
                        tvConfirmacion.visibility = View.VISIBLE
                        tvConfirmacion.text = "✓ ¡Añadido a tus componentes locales de TORRECOMS!"
                        Toast.makeText(this@DetalleProductoActivity, "Componente guardado en tu base de datos local", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@DetalleProductoActivity, "Error al guardar localmente", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

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

    private fun mostrarProducto(p: ProductoOnline) {
        tvTitulo.text = p.title
        tvMarca.text = p.brand ?: "Marca oficial"
        tvCategoria.text = "Categoría: ${p.category}"
        tvPrecio.text = "${String.format(Locale.US, "%.2f", p.price)} €"
        tvStock.text = if (p.stock > 0) "En stock (${p.stock} unidades)" else "Agotado"
        tvRating.text = "★ Puntuación: ${String.format(Locale.US, "%.1f", p.rating)} / 5.0"
        tvDescripcion.text = p.description

        // Carga de imagen con Glide
        Glide.with(this)
            .load(p.thumbnail)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .fitCenter()
            .into(ivFoto)
    }
}
