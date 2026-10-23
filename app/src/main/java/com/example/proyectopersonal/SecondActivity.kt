package com.example.proyectopersonal

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity

class SecondActivity : ComponentActivity() {

    private var reproductor: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val textoNombre = findViewById<TextView>(R.id.textoNombre)
        val textoDetalle = findViewById<TextView>(R.id.textoDetalle)
        val imagenResultado = findViewById<ImageView>(R.id.imagenResultado)
        val botonVolver = findViewById<Button>(R.id.botonVolver)

        val nombre = intent.getStringExtra("NOMBRE_EXTRA") ?: "Sin nombre"
        val precio = intent.getDoubleExtra("PRECIO_EXTRA", 0.0)
        val categoria = intent.getStringExtra("CATEGORIA_EXTRA") ?: "Otro"

        textoNombre.text = nombre
        textoDetalle.text = "Categoría: $categoria\nPrecio: $precio €"

        when (categoria.uppercase()) {
            "CPU" -> imagenResultado.setImageResource(R.drawable.img_cpu)
            "GPU" -> imagenResultado.setImageResource(R.drawable.img_gpu)
            "RAM" -> imagenResultado.setImageResource(R.drawable.img_ram)
            "DISCO" -> imagenResultado.setImageResource(R.drawable.img_storage)
            else -> imagenResultado.setImageResource(R.drawable.img_other)
        }

        val sonidoId: Int
        when {
            precio < 450 -> sonidoId = R.raw.sonido1
            precio > 450 -> sonidoId = R.raw.sonido2
            else -> sonidoId = R.raw.sonido3
        }

        reproductor = MediaPlayer.create(this, sonidoId)
        reproductor?.start()

        botonVolver.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        reproductor?.release()
        reproductor = null
    }
}