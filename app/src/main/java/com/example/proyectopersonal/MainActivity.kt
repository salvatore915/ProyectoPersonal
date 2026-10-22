package com.example.proyectopersonal
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editNombre = findViewById<EditText>(R.id.editNombre)
        val editPrecio = findViewById<EditText>(R.id.editPrecio)
        val spinnerCategoria = findViewById<Spinner>(R.id.spinnerCategoria)
        val boton = findViewById<Button>(R.id.botonAgregar)
        val botonVerHistorial = findViewById<Button>(R.id.botonVerHistorial)

        val prefs = getSharedPreferences("MisDatos", MODE_PRIVATE)
        val nombreGuardado = prefs.getString("NOMBRE_GUARDADO", "")
        editNombre.setText(nombreGuardado)


        val precioGuardado = prefs.getString("PRECIO_GUARDADO", "")
        editPrecio.setText(precioGuardado)

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
                    prefs.edit().putString("NOMBRE_GUARDADO", nombre).putString("PRECIO_GUARDADO", precioTexto).apply()

                    val precio = precioTexto.toDouble()
                    Historial.componentes.add(Componente(nombre, precio, categoria))

                    val intent = Intent(this, SecondActivity::class.java)
                    intent.putExtra("NOMBRE_EXTRA", nombre)
                    intent.putExtra("PRECIO_EXTRA", precio)
                    intent.putExtra("CATEGORIA_EXTRA", categoria)
                    startActivity(intent)
                }
            }
        }

        botonVerHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }
    }

    // onResume se ejecuta cada vez que vuelves a esta pantalla, así el resumen siempre está al día
    override fun onResume() {
        super.onResume()
        val textoResumen = findViewById<TextView>(R.id.textoResumen)
        val cantidad = Historial.componentes.size
        val total = String.format("%.2f", Historial.obtenerTotalPrecio())
        textoResumen.text = "Componentes: $cantidad | Total: $total €"
    }
}