package com.example.proyectopersonal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProductoOnlineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(productos: List<ProductoOnline>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: ProductoOnline)

    @Query("SELECT * FROM productos_online ORDER BY id ASC")
    suspend fun obtenerTodos(): List<ProductoOnline>

    @Query("SELECT * FROM productos_online WHERE id = :id")
    suspend fun obtenerPorId(id: Int): ProductoOnline?

    @Query("SELECT * FROM productos_online WHERE category = :cat ORDER BY id ASC")
    suspend fun obtenerPorCategoria(cat: String): List<ProductoOnline>

    @Query("SELECT * FROM productos_online WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%'")
    suspend fun buscar(query: String): List<ProductoOnline>

    @Query("DELETE FROM productos_online")
    suspend fun borrarTodos()

    @Query("SELECT COUNT(*) FROM productos_online")
    suspend fun contar(): Int
}
