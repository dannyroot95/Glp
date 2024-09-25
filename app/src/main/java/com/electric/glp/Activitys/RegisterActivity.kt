package com.electric.glp.Activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.electric.glp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    // Variable para ViewBinding
    private lateinit var binding: ActivityRegisterBinding

    // Variable para FirebaseAuth y FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        // Inicializar ViewBinding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase Auth y Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Acciones del botón de registro
        binding.registerButton.setOnClickListener {
            val nombres = binding.nombresEditText.text.toString().trim()
            val apellidos = binding.apellidosEditText.text.toString().trim()
            val organizacion = binding.organizacionEditText.text.toString().trim()
            val correo = binding.correoEditText.text.toString().trim()
            val contrasena = binding.contrasenaEditText.text.toString().trim()

            // Validación de campos vacíos
            if (nombres.isEmpty() || apellidos.isEmpty() || organizacion.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Deshabilitar inputs y mostrar ProgressBar
            setLoadingState(true)

            // Registrar nuevo usuario con correo y contraseña
            auth.createUserWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        // Guardar información adicional en Firestore
                        val userId = user!!.uid
                        val userInfo = hashMapOf(
                            "nombres" to nombres,
                            "apellidos" to apellidos,
                            "organizacion" to organizacion,
                            "email" to correo,
                            "id" to userId,
                            "deviceId" to "",
                            "typeLogin" to "Email",
                            "notificationStatus" to false,
                            "monitoringInSeconds" to 300000,
                            "modeRegisters" to "a"

                        )

                        // Guardar el documento en la colección "users" en Firestore
                        firestore.collection("users").document(userId).set(userInfo)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registro exitoso!", Toast.LENGTH_SHORT).show()

                                // Redirigir a MainActivity
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al registrar los datos!", Toast.LENGTH_SHORT).show()
                                setLoadingState(false)
                            }
                    } else {
                        Toast.makeText(this, "Error al registrar!", Toast.LENGTH_SHORT).show()
                        setLoadingState(false)
                    }
                }
        }
    }

    // Función para controlar el estado de carga
    private fun setLoadingState(isLoading: Boolean) {
        // Deshabilitar los campos y botón mientras se muestra el ProgressBar
        binding.nombresEditText.isEnabled = !isLoading
        binding.apellidosEditText.isEnabled = !isLoading
        binding.organizacionEditText.isEnabled = !isLoading
        binding.correoEditText.isEnabled = !isLoading
        binding.contrasenaEditText.isEnabled = !isLoading
        binding.registerButton.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}