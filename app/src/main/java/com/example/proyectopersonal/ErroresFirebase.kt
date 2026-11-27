package com.example.proyectopersonal

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException

// Traduce los errores de Firebase a mensajes en español
object ErroresFirebase {
    fun mensaje(error: Exception?): String = when (error) {
        is FirebaseNetworkException -> "No hay conexión a Internet"
        is FirebaseTooManyRequestsException -> "Demasiados intentos. Prueba más tarde"
        is FirebaseAuthException -> when (error.errorCode) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Ese correo ya tiene una cuenta"
            "ERROR_INVALID_EMAIL" -> "El correo no es válido"
            "ERROR_WEAK_PASSWORD" -> "La contraseña debe tener al menos 6 caracteres"
            "ERROR_INVALID_CREDENTIAL",
            "ERROR_WRONG_PASSWORD",
            "ERROR_USER_NOT_FOUND" -> "Correo o contraseña incorrectos"
            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Prueba más tarde"
            else -> "Algo ha fallado. Inténtalo de nuevo"
        }
        else -> "Algo ha fallado. Inténtalo de nuevo"
    }
}