package com.example.proyectopersonal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.Locale

// Adaptador que conecta la lista de productos online con el RecyclerView y gestiona clics
class CatalogoAdapter(
    private var productos: List<ProductoOnline>,
    private val onItemClick: (ProductoOnline) -> Unit
) : RecyclerView.Adapter<CatalogoAdapter.ProductoViewHolder>() {

    // Guarda las referencias a las vistas de cada fila para reutilizarlas al hacer scroll
    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagen: ImageView = view.findViewById(R.id.ivProductoOnline)
        val tvTitulo: TextView = view.findViewById(R.id.tvTituloOnline)
        val tvMarcaCategoria: TextView = view.findViewById(R.id.tvMarcaCategoriaOnline)
        val tvValoracion: TextView = view.findViewById(R.id.tvValoracionOnline)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecioOnline)
    }

    // Infla el diseño XML de una fila y crea su ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.fila_producto_online, parent, false)
        return ProductoViewHolder(vista)
    }

    // Rellena los datos y la imagen del producto correspondiente a la posición actual
    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val item = productos[position]

        holder.tvTitulo.text = item.title
        holder.tvMarcaCategoria.text = "${item.brand ?: "Marca"} • ${item.category}"
        holder.tvValoracion.text = "★ ${String.format(Locale.US, "%.1f", item.rating)} | Stock: ${item.stock}"
        holder.tvPrecio.text = "${String.format(Locale.US, "%.2f", item.price)} €"

        // Carga de imagen remota con Glide
        Glide.with(holder.itemView.context)
            .load(item.thumbnail)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .centerCrop()
            .into(holder.imagen)

        // Notifica a la actividad qué producto se ha pulsado
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    // Devuelve el número total de elementos mostrados en la lista
    override fun getItemCount(): Int = productos.size

    // Sustituye los datos actuales y refresca la vista del RecyclerView
    fun actualizarLista(nuevaLista: List<ProductoOnline>) {
        this.productos = nuevaLista
        notifyDataSetChanged()
    }
}