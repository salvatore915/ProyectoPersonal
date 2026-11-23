package com.example.proyectopersonal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// DAO de Room que define las operaciones de caché y consulta sobre productos_online
@Dao
interface ProductoOnlineDao {

    // Inserta una lista de productos y sobrescribe los que ya existan con el mismo ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(productos: List<ProductoOnline>)

    // Inserta un producto individual o reemplaza el registro si ya existe en la tabla
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: ProductoOnline)

    // Devuelve todos los productos guardados ordenados por ID ascendente
    @Query("SELECT * FROM productos_online ORDER BY id ASC")
    suspend fun obtenerTodos(): List<ProductoOnline>

    // Busca y devuelve un producto concreto por su ID o null si no existe
    @Query("SELECT * FROM productos_online WHERE id = :id")
    suspend fun obtenerPorId(id: Int): ProductoOnline?

    // Filtra y devuelve los productos de una categoría concreta ordenados por ID ascendente
    @Query("SELECT * FROM productos_online WHERE category = :cat ORDER BY id ASC")
    suspend fun obtenerPorCategoria(cat: String): List<ProductoOnline>

    // Busca texto coincidente en título, descripción o marca para consultas sin conexión
    @Query("SELECT * FROM productos_online WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%'")
    suspend fun buscar(query: String): List<ProductoOnline>
}