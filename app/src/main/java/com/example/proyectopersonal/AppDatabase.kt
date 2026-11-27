package com.example.proyectopersonal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Declara la base de datos de Room con sus tablas y versión actual
@Database(entities = [Componente::class, ProductoOnline::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Expone los DAOs para realizar consultas e inserciones en cada tabla
    abstract fun componenteDao(): ComponenteDao
    abstract fun productoOnlineDao(): ProductoOnlineDao

    companion object {
        // Garantiza que los cambios en instancia sean visibles al instante entre hilos
        @Volatile
        private var instancia: AppDatabase? = null

        // Devuelve la única instancia de la base de datos bajo el patrón Singleton
        fun obtener(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                // Evita que dos hilos creen la base de datos a la vez
                val nueva = Room.databaseBuilder(
                                context.applicationContext, // Usa el contexto de aplicación para evitar fugas de memoria
                                AppDatabase::class.java,
                                "componentes_db" // Nombre del archivo de base de datos en el dispositivo
                            ).fallbackToDestructiveMigration(false).build() // Recrea tablas si sube la versión sin migración manual
                instancia = nueva
                nueva
            }
        }
    }
}