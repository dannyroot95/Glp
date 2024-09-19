package com.electric.glp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.electric.glp.databinding.ActivityMainBinding
import com.electric.glp.databinding.ActivityMenuBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ActivityMenu : AppCompatActivity() {

    private lateinit var binding : ActivityMenuBinding
    private var database: DatabaseReference = FirebaseDatabase.getInstance().getReference("device")
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            clearSpecificPreferences()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        getParametersSensor(database)
    }


    private fun getParametersSensor(database: DatabaseReference) {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null)
        database.child(deviceId!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val glpValue = snapshot.child("glp").getValue(Int::class.java)
                Toast.makeText(this@ActivityMenu,"Valor glp : $glpValue", Toast.LENGTH_SHORT).show()
            }
            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value: ${error.message}")
            }
        })
    }

    private fun clearSpecificPreferences() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            remove("userId")   // Borra solo el userId
            remove("deviceId") // Borra solo el deviceId
            apply()            // Aplica los cambios de manera asincrónica
        }
    }


    override fun onBackPressed() {
    }

}