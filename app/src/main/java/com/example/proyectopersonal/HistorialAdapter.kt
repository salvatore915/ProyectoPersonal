package com.example.proyectopersonal

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageButton

// Adaptador que muestra la lista de componentes locales en el RecyclerView y gestiona clics o borrados
class HistorialAdapter(
    private val componentes: MutableList<Componente>,
    private val onItemDeleted: (Componente) -> Unit
) : RecyclerView.Adapter<HistorialAdapter.ComponenteViewHolder>() {

    // Guarda las referencias a las vistas de cada fila del historial
    class ComponenteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagenCategoria: ImageView = view.findViewById(R.id.imagenCategoriaFila)
        val ivFoto: ImageView = view.findViewById(R.id.ivFoto)
        val textoNombre: TextView = view.findViewById(R.id.textoNombreFila)
        val textoCategoria: TextView = view.findViewById(R.id.textoCategoriaFila)
        val textoPrecio: TextView = view.findViewById(R.id.textoPrecioFila)
        val botonEliminar: ImageButton = view.findViewById(R.id.botonEliminarFila)
    }

    // Infla el diseño XML de una fila y crea su ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComponenteViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.fila_componente, parent, false)
        return ComponenteViewHolder(vista)
    }

    // Rellena los datos, imágenes y acciones de cada fila según su posición
    override fun onBindViewHolder(holder: ComponenteViewHolder, position: Int) {
        val componente = componentes[position]
        holder.textoNombre.text = componente.nombre
        holder.textoCategoria.text = "Categoría: ${componente.categoria}"
        holder.textoPrecio.text = "${componente.precio} €"

        // Recupera la foto de perfil guardada en SharedPreferences o una alternativa de la lista
        val context = holder.itemView.context
        val prefs = context.getSharedPreferences("MisDatos", Context.MODE_PRIVATE)
        val fotoPerfilPrefs = prefs.getString("FOTO_PERFIL", null)

        val fotoPerfilPersona = fotoPerfilPrefs ?: componentes.firstOrNull {
            it.categoria.uppercase() in listOf("CPU", "GPU", "RAM", "DISCO") && !it.fotoPath.isNullOrBlank()
        }?.fotoPath

        // Carga los bitmaps de la foto de respaldo y de la foto propia del componente
        val bitmapPerfilPersona = Fotos.cargar(fotoPerfilPersona)
        val bitmapComponente = Fotos.cargar(componente.fotoPath)
        val categoria = (componente.categoria ?: "").uppercase()

        // Asigna el icono de categoría y decide qué imagen mostrar en cada ImageView
        if (categoria in listOf("CPU", "GPU", "RAM", "DISCO")) {
            when (categoria) {
                "CPU" -> holder.imagenCategoria.setImageResource(R.drawable.img_cpu)
                "GPU" -> holder.imagenCategoria.setImageResource(R.drawable.img_gpu)
                "RAM" -> holder.imagenCategoria.setImageResource(R.drawable.img_ram)
                "DISCO" -> holder.imagenCategoria.setImageResource(R.drawable.img_storage)
            }
            holder.imagenCategoria.visibility = View.VISIBLE

            val fotoPersona = bitmapComponente ?: bitmapPerfilPersona
            if (fotoPersona != null) {
                holder.ivFoto.setImageBitmap(fotoPersona)
                holder.ivFoto.visibility = View.VISIBLE
            } else {
                holder.ivFoto.visibility = View.GONE
            }
        } else {
            if (bitmapComponente != null) {
                holder.imagenCategoria.setImageBitmap(bitmapComponente)
            } else {
                holder.imagenCategoria.setImageResource(R.drawable.img_other)
            }
            holder.imagenCategoria.visibility = View.VISIBLE

            if (bitmapPerfilPersona != null) {
                holder.ivFoto.setImageBitmap(bitmapPerfilPersona)
                holder.ivFoto.visibility = View.VISIBLE
            } else {
                holder.ivFoto.visibility = View.GONE
            }
        }

        // Abre SecondActivity pasando los datos del componente pulsado mediante extras
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, SecondActivity::class.java)
            intent.putExtra("NOMBRE_EXTRA", componente.nombre)
            intent.putExtra("PRECIO_EXTRA", componente.precio)
            intent.putExtra("CATEGORIA_EXTRA", componente.categoria)
            intent.putExtra("FOTO_EXTRA", componente.fotoPath)
            holder.itemView.context.startActivity(intent)
        }

        // Verifica la posición actual de forma segura y notifica el borrado del componente
        holder.botonEliminar.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION && pos < componentes.size) {
                onItemDeleted(componentes[pos])
            }
        }
    }

    // Devuelve la cantidad total de componentes en el adaptador
    override fun getItemCount(): Int = componentes.size
}