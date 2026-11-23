package com.example.proyectopersonal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

// Utilidad singleton para crear, copiar, cargar y exponer archivos de fotos en el almacenamiento interno
object Fotos {
    // Crea y devuelve un archivo temporal JPG dentro de la carpeta fotos de la app
    fun crearArchivo(context: Context): File {
        val directorio = File(context.filesDir, "fotos")
        if (!directorio.exists()) {
            directorio.mkdirs()
        }
        return File.createTempFile("foto_", ".jpg", directorio)
    }

    // Genera una Uri segura mediante FileProvider para compartir el archivo con otras apps o cámara
    fun uriDe(context: Context, archivo: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
    }

    // Copia el contenido de una Uri externa a un archivo interno y devuelve su ruta absoluta
    fun copiarDesdeUri(context: Context, uri: Uri): String? {
        return try {
            val archivo = crearArchivo(context)
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(archivo)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            archivo.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Lee un archivo desde su ruta y lo decodifica como Bitmap si existe
    fun cargar(path: String?): Bitmap? {
        if (path == null) return null
        val archivo = File(path)
        if (!archivo.exists()) return null
        return BitmapFactory.decodeFile(path)
    }
}