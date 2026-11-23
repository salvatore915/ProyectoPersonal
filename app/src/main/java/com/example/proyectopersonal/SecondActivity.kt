package com.example.proyectopersonal

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

// Actividad que muestra el resumen de un componente, su imagen o icono y reproduce un sonido según el precio
class SecondActivity : AppCompatActivity() {

    // Reproductor multimedia para el efecto de sonido al mostrar el componente
    private var reproductor: MediaPlayer? = null

    // Configura la vista, recupera los datos del intent, muestra la imagen y reproduce el audio
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        // Obtiene las vistas de texto, imagen y botón de regreso
        val textoNombre = findViewById<TextView>(R.id.textoNombre)
        val textoDetalle = findViewById<TextView>(R.id.textoDetalle)
        val imagenResultado = findViewById<ImageView>(R.id.imagenResultado)
        val botonVolver = findViewById<Button>(R.id.botonVolver)

        // Recupera los datos enviados desde la pantalla anterior
        val nombre = intent.getStringExtra("NOMBRE_EXTRA") ?: "Sin nombre"
        val precio = intent.getDoubleExtra("PRECIO_EXTRA", 0.0)
        val categoria = intent.getStringExtra("CATEGORIA_EXTRA") ?: "Otro"
        val fotoPath = intent.getStringExtra("FOTO_EXTRA")

        // Muestra el nombre, la categoría y el precio del componente
        textoNombre.text = nombre
        textoDetalle.text = "Categoría: $categoria\nPrecio: $precio €"

        // Asigna el dibujo según la categoría, o la foto si no es una categoría estándar
        when (categoria.uppercase()) {
            "CPU"   -> imagenResultado.setImageResource(R.drawable.img_cpu)
            "GPU"   -> imagenResultado.setImageResource(R.drawable.img_gpu)
            "RAM"   -> imagenResultado.setImageResource(R.drawable.img_ram)
            "DISCO" -> imagenResultado.setImageResource(R.drawable.img_storage)
            else -> {
                val bitmap = Fotos.cargar(fotoPath)
                if (bitmap != null) {
                    imagenResultado.setImageBitmap(bitmap)
                } else {
                    imagenResultado.setImageResource(R.drawable.img_other)
                }
            }
        }

        // Elige un sonido distinto según el umbral de precio del componente
        val sonidoId: Int
        when {
            precio < 450 -> sonidoId = R.raw.sonido1
            precio > 450 -> sonidoId = R.raw.sonido2
            else -> sonidoId = R.raw.sonido2
        }

        // Crea e inicia la reproducción del sonido seleccionado
        reproductor = MediaPlayer.create(this, sonidoId)
        reproductor?.start()

        // Cierra la actividad y vuelve a la pantalla anterior
        botonVolver.setOnClickListener {
            finish()
        }
    }

    // Libera los recursos del MediaPlayer cuando se destruye la actividad
    override fun onDestroy() {
        super.onDestroy()
        reproductor?.release()
        reproductor = null
    }
}