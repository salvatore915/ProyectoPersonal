package com.example.proyectopersonal
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editNombre = findViewById<EditText>(R.id.editNombre)
        val editPrecio = findViewById<EditText>(R.id.editPrecio)
        val spinnerCategoria = findViewById<Spinner>(R.id.spinnerCategoria)
        val boton = findViewById<Button>(R.id.botonAgregar)
        val botonVerHistorial = findViewById<Button>(R.id.botonVerHistorial)

        val dao = AppDatabase.obtener(this).componenteDao()

        val prefs = getSharedPreferences("MisDatos", MODE_PRIVATE)
        val nombreGuardado = prefs.getString("NOMBRE_GUARDADO", "")
        editNombre.setText(nombreGuardado)

        val precioGuardado = prefs.getString("PRECIO_GUARDADO", "")
        editPrecio.setText(precioGuardado)

        val categoriaGuardada = prefs.getInt("CATEGORIA_GUARDADA", 0)
        spinnerCategoria.setSelection(categoriaGuardada)

        boton.setOnClickListener {
            val nombre = editNombre.text.toString()
            val precioTexto = editPrecio.text.toString()
            val categoria = spinnerCategoria.selectedItem.toString()

            when {
                nombre.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.error_nombre_vacio), Toast.LENGTH_SHORT).show()
                }
                precioTexto.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.error_precio_vacio), Toast.LENGTH_SHORT).show()
                }
                precioTexto.toDoubleOrNull() == null -> {
                    Toast.makeText(this, getString(R.string.error_precio_invalido), Toast.LENGTH_SHORT).show()
                }
                precioTexto.toDouble() <= 0.0 -> {
                    Toast.makeText(this, getString(R.string.error_precio_invalido), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    prefs.edit().putString("NOMBRE_GUARDADO", nombre).putString("PRECIO_GUARDADO", precioTexto).putInt("CATEGORIA_GUARDADA", spinnerCategoria.selectedItemPosition).apply()

                    val precio = precioTexto.toDouble()
                    val nuevoComponente = Componente(nombre = nombre, precio = precio, categoria = categoria)

                    lifecycleScope.launch {
                        dao.insertar(nuevoComponente)

                        val intent = Intent(this@MainActivity, SecondActivity::class.java)
                        intent.putExtra("NOMBRE_EXTRA", nombre)
                        intent.putExtra("PRECIO_EXTRA", precio)
                        intent.putExtra("CATEGORIA_EXTRA", categoria)
                        startActivity(intent)
                    }
                }
            }
        }

        botonVerHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }
    }

    // Se actualiza el resumen consultando la base de datos cada vez que la pantalla vuelve a ser visible[cite: 4, 5]
    override fun onResume() {
        super.onResume()
        val textoResumen = findViewById<TextView>(R.id.textoResumen)
        val dao = AppDatabase.obtener(this).componenteDao()
        lifecycleScope.launch {
            val lista = dao.obtenerTodos()
            val cantidad = lista.size
            val total = lista.sumOf { it.precio }
            val totalFormateado = String.format("%.2f", total)
            textoResumen.text = "Componentes: $cantidad | Total: $totalFormateado €"
        }
    }
}