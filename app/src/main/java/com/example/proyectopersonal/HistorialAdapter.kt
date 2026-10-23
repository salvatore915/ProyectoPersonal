package com.example.proyectopersonal
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageButton

class HistorialAdapter(private val componentes: List<Componente>) :
    RecyclerView.Adapter<HistorialAdapter.ComponenteViewHolder>() {

    class ComponenteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagenCategoria: ImageView = view.findViewById(R.id.imagenCategoriaFila)
        val textoNombre: TextView = view.findViewById(R.id.textoNombreFila)
        val textoCategoria: TextView = view.findViewById(R.id.textoCategoriaFila)
        val textoPrecio: TextView = view.findViewById(R.id.textoPrecioFila)
        val botonEliminar: ImageButton = view.findViewById(R.id.botonEliminarFila)   // <- nuevo boton para eliminar
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComponenteViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.fila_componente, parent, false)
        return ComponenteViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ComponenteViewHolder, position: Int) {
        val componente = componentes[position]
        holder.textoNombre.text = componente.nombre
        holder.textoCategoria.text = "Categoría: ${componente.categoria}"
        holder.textoPrecio.text = "${componente.precio} €"

        val categoria = componente.categoria.uppercase()
        when (categoria) {
            "CPU" -> holder.imagenCategoria.setImageResource(R.drawable.img_cpu)
            "GPU" -> holder.imagenCategoria.setImageResource(R.drawable.img_gpu)
            "RAM" -> holder.imagenCategoria.setImageResource(R.drawable.img_ram)
            "DISCO" -> holder.imagenCategoria.setImageResource(R.drawable.img_storage)
            else -> holder.imagenCategoria.setImageResource(R.drawable.img_other)
        }

        // Al tocar una fila se abre SecondActivity con los datos de ese componente
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, SecondActivity::class.java)
            intent.putExtra("NOMBRE_EXTRA", componente.nombre)
            intent.putExtra("PRECIO_EXTRA", componente.precio)
            intent.putExtra("CATEGORIA_EXTRA", componente.categoria)
            holder.itemView.context.startActivity(intent)
        }

        //nuevo holder para realizar la operacion de borrado
        holder.botonEliminar.setOnClickListener {
            Historial.componentes.removeAt(position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = componentes.size
}