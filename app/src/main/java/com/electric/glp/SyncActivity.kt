package com.electric.glp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.electric.glp.databinding.ActivitySyncBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator

class SyncActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySyncBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivitySyncBinding.inflate(layoutInflater)
        setContentView(binding.root)


            binding.buttonScanQR.setOnClickListener {
                val integrator = IntentIntegrator(this).apply {
                    setOrientationLocked(true)
                    setPrompt("Enfoque un código QR en el recuadro")
                    captureActivity = CaptureActivityPortrait::class.java
                    initiateScan()
                }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Scan: ${result.contents}", Toast.LENGTH_LONG).show()
                // Guarda el resultado como deviceId
                saveDeviceIdInPreferences(result.contents)
                // Redirige a ActivityMenu
                saveToFirestore(result.contents)

            }
        }
    }

    private fun saveDeviceIdInPreferences(deviceId: String) {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString("deviceId", deviceId)
            apply()
        }
    }
    private fun getUserDetailsFromPreferences(): Pair<String?, String?> {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", "")   // Retorna null si no existe la clave "userId"
        val deviceId = prefs.getString("deviceId", "") // Retorna null si no existe la clave "deviceId"
        return Pair(userId, deviceId)
    }


    override fun onBackPressed() {
    }

    override fun onStart() {
        super.onStart()
        val (userId, deviceId) = getUserDetailsFromPreferences()
        if (deviceId != "") {
            // No se encontraron datos, posiblemente redirigir al usuario para iniciar sesión
            startActivity(Intent(this, ActivityMenu::class.java))
            finish()
        }
    }

    private fun saveToFirestore(scannedDeviceId : String){
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", "")

        val firestore = FirebaseFirestore.getInstance()
        val userDocRef = firestore.collection("users").document(userId!!)

        binding.progressBar.visibility = View.VISIBLE
        binding.buttonScanQR.visibility = View.GONE

        userDocRef.update("deviceId", scannedDeviceId)
            .addOnSuccessListener {
                Toast.makeText(this, "Dispositivo guardado!", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.buttonScanQR.visibility = View.VISIBLE
                Toast.makeText(this, "Error al actualizar dispositivo: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

}