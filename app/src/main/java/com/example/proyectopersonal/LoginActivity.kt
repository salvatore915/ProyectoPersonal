package com.example.proyectopersonal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

// Pantalla de inicio de sesión (de momento solo abre el registro)
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.btnIrRegistro).setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }
}