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

// Pantalla de arranque: entra directo si hay sesión o pide correo y contraseña
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var btnIniciarSesion: Button
    private lateinit var btnIrRegistro: Button
    private lateinit var progreso: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // Firebase recuerda la sesión: si hay usuario, no se muestra Login
        if (auth.currentUser != null) {
            abrirPrincipal()
            return
        }

        setContentView(R.layout.activity_login)

        val editCorreo = findViewById<EditText>(R.id.editCorreoLogin)
        val editPassword = findViewById<EditText>(R.id.editPasswordLogin)
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion)
        btnIrRegistro = findViewById(R.id.btnIrRegistro)
        progreso = findViewById(R.id.progressLogin)

        btnIniciarSesion.setOnClickListener {
            val correo = editCorreo.text.toString().trim()
            val password = editPassword.text.toString()

            // Validación local: si algo falla, no se llama a Firebase
            when {
                correo.isEmpty() || password.isEmpty() ->
                    avisar("Rellena todos los campos")
                !Patterns.EMAIL_ADDRESS.matcher(correo).matches() ->
                    avisar("El correo no es válido")
                else -> iniciarSesion(correo, password)
            }
        }

        btnIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    // Operación asíncrona: Authentication comprueba las credenciales
    private fun iniciarSesion(correo: String, password: String) {
        mostrarCarga(true)
        auth.signInWithEmailAndPassword(correo, password)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    abrirPrincipal()
                } else {
                    mostrarCarga(false)
                    avisar(ErroresFirebase.mensaje(tarea.exception))
                }
            }
    }

    // Abre la principal y borra Login del historial
    private fun abrirPrincipal() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Muestra u oculta la carga y bloquea los botones mientras se espera
    private fun mostrarCarga(cargando: Boolean) {
        progreso.visibility = if (cargando) View.VISIBLE else View.GONE
        btnIniciarSesion.isEnabled = !cargando
        btnIrRegistro.isEnabled = !cargando
    }

    private fun avisar(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}