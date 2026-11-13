package com.example.proyectopersonal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.Locale

class CatalogoAdapter(
    private var productos: List<ProductoOnline>,
    private val onItemClick: (ProductoOnline) -> Unit
) : RecyclerView.Adapter<CatalogoAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagen: ImageView = view.findViewById(R.id.ivProductoOnline)
        val tvTitulo: TextView = view.findViewById(R.id.tvTituloOnline)
        val tvMarcaCategoria: TextView = view.findViewById(R.id.tvMarcaCategoriaOnline)
        val tvValoracion: TextView = view.findViewById(R.id.tvValoracionOnline)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecioOnline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.fila_producto_online, parent, false)
        return ProductoViewHolder(vista)
    }

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

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = productos.size

    fun actualizarLista(nuevaLista: List<ProductoOnline>) {
        this.productos = nuevaLista
        notifyDataSetChanged()
    }
}
