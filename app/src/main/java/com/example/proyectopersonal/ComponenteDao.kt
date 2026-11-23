package com.example.proyectopersonal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

// DAO de Room que define las operaciones sobre la tabla de componentes
@Dao
interface ComponenteDao {
    // Inserta un nuevo componente en la base de datos de forma asíncrona
    @Insert
    suspend fun insertar(componente: Componente)

    // Devuelve todos los componentes ordenados del más reciente al más antiguo
    @Query("SELECT * FROM componentes ORDER BY id DESC")
    suspend fun obtenerTodos(): List<Componente>

    // Elimina un componente concreto de la tabla
    @Delete
    suspend fun borrar(componente: Componente)

    // Devuelve el número total de componentes registrados
    @Query("SELECT COUNT(*) FROM componentes")
    suspend fun contarComponentes(): Int

    // Filtra y devuelve los componentes que pertenecen a una categoría concreta
    @Query("SELECT * FROM componentes WHERE categoria = :cat ORDER BY id DESC")
    suspend fun obtenerPorCategoria(cat: String): List<Componente>
}