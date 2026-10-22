package com.example.proyectopersonal

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistorialActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        val textoResumen = findViewById<TextView>(R.id.textoResumenHistorial)
        val recycler = findViewById<RecyclerView>(R.id.recyclerHistorial)
        val botonVolver = findViewById<Button>(R.id.botonVolverHistorial)

        val cantidad = Historial.componentes.size
        val total = String.format("%.2f", Historial.obtenerTotalPrecio())
        textoResumen.text = "Total: $cantidad componentes | $total €"

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = HistorialAdapter(Historial.componentes)

        botonVolver.setOnClickListener {
            finish()
        }
    }
}