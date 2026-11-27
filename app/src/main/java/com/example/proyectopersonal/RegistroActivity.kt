package com.example.proyectopersonal

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// Pantalla para crear una cuenta con correo y contraseña
class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var btnCrearCuenta: Button
    private lateinit var btnVolverLogin: Button
    private lateinit var progreso: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val editNombre = findViewById<EditText>(R.id.editNombreRegistro)
        val editCorreo = findViewById<EditText>(R.id.editCorreoRegistro)
        val editPassword = findViewById<EditText>(R.id.editPasswordRegistro)
        val editRepetir = findViewById<EditText>(R.id.editRepetirPassword)
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta)
        btnVolverLogin = findViewById(R.id.btnVolverLogin)
        progreso = findViewById(R.id.progressRegistro)

        btnCrearCuenta.setOnClickListener {
            val nombre = editNombre.text.toString().trim()
            val correo = editCorreo.text.toString().trim()
            val password = editPassword.text.toString()
            val repetida = editRepetir.text.toString()

            // Validación local: si algo falla, no se llama a Firebase
            when {
                nombre.isEmpty() || correo.isEmpty() || password.isEmpty() || repetida.isEmpty() ->
                    avisar("Rellena todos los campos")
                !Patterns.EMAIL_ADDRESS.matcher(correo).matches() ->
                    avisar("El correo no es válido")
                password.length < 6 ->
                    avisar("La contraseña debe tener al menos 6 caracteres")
                password != repetida ->
                    avisar("Las contraseñas no coinciden")
                else -> crearCuenta(nombre, correo, password)
            }
        }

        // Cierra el registro y vuelve a Login
        btnVolverLogin.setOnClickListener { finish() }
    }

    // Paso 1: Authentication crea la cuenta (operación asíncrona)
    private fun crearCuenta(nombre: String, correo: String, password: String) {
        mostrarCarga(true)
        auth.createUserWithEmailAndPassword(correo, password)
            .addOnCompleteListener { tarea ->
                val uid = if (tarea.isSuccessful) tarea.result.user?.uid else null
                if (uid != null) {
                    guardarPerfil(uid, nombre, correo)
                } else {
                    mostrarCarga(false)
                    avisar(ErroresFirebase.mensaje(tarea.exception))
                }
            }
    }

    // Paso 2: guarda el perfil en usuarios/{uid}. La contraseña nunca se guarda
    private fun guardarPerfil(uid: String, nombre: String, correo: String) {
        val perfil = hashMapOf(
            "nombre" to nombre,
            "email" to correo,
            "fechaRegistro" to FieldValue.serverTimestamp()
        )
        db.collection("usuarios").document(uid).set(perfil)
            .addOnSuccessListener { abrirPrincipal() }
            .addOnFailureListener {
                mostrarCarga(false)
                avisar("La cuenta se ha creado, pero no se ha guardado el perfil")
            }
    }

    // Paso 3: abre la principal y borra Login y Registro del historial
    private fun abrirPrincipal() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    // Muestra u oculta la carga y bloquea los botones mientras se espera
    private fun mostrarCarga(cargando: Boolean) {
        progreso.visibility = if (cargando) View.VISIBLE else View.GONE
        btnCrearCuenta.isEnabled = !cargando
        btnVolverLogin.isEnabled = !cargando
    }

    private fun avisar(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}