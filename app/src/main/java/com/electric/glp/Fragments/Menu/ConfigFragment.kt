package com.electric.glp.Fragments.Menu

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.electric.glp.R
import com.electric.glp.databinding.FragmentConfigBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class ConfigFragment : Fragment(R.layout.fragment_config), OnMapReadyCallback {
    private var _binding: FragmentConfigBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentConfigBinding.inflate(inflater, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserData()
        setupEventHandlers()
    }

    private fun loadUserData() {

        val userId = context?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)?.getString("userId", null)
        if (userId != null) {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
            userRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Log the error
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val nombres = snapshot.getString("nombres")
                    val apellidos = snapshot.getString("apellidos")
                    val organizacion = snapshot.getString("organizacion")
                    val email = snapshot.getString("email")
                    val typeLogin = snapshot.getString("typeLogin")
                    val deviceId = snapshot.getString("deviceId")
                    val monitoringInSeconds = snapshot.get("monitoringInSeconds").toString().toInt()/1000
                    val modeRegisters = snapshot.getString("modeRegisters")
                    val notificationStatus = snapshot.getBoolean("notificationStatus") ?: false

                    val latitude = snapshot.getDouble("lat")!!
                    val longitude = snapshot.getDouble("lon")!!

                    loadUserData(latitude,longitude)

                    binding.nombresEditText.setText(nombres)
                    binding.apellidosEditText.setText(apellidos)
                    binding.organizacionEditText.setText(organizacion)
                    binding.correoEditText.setText(email)
                    binding.timeInMinutes.setText(monitoringInSeconds.toString())
                    binding.typeLogin.text = typeLogin
                    binding.serieDevice.text = "Serie de dispositivo: ${deviceId}"

                    if(modeRegisters == "a"){
                        binding.chkManual.isChecked = false
                        binding.chkAuto.isChecked = true
                    }else{
                        binding.chkManual.isChecked = true
                        binding.chkAuto.isChecked = false
                    }


                    binding.notificationSwitch.isChecked = notificationStatus

                    binding.linearLottie3.visibility = View.GONE
                    binding.linearFragment3.visibility = View.VISIBLE



                }
            }
        }
    }

    private fun updateUserData() {
        val userId = context?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)?.getString("userId", null)
        userId?.let {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(it)
            val newData = mapOf(
                "nombres" to binding.nombresEditText.text.toString(),
                "apellidos" to binding.apellidosEditText.text.toString(),
                "organizacion" to binding.organizacionEditText.text.toString()
            )
            userRef.update(newData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(binding.root.context,"Datos actualizados!",Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(binding.root.context,"Error al actualizar datos!",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateMonitoringSettings() {
        val userId = context?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)?.getString("userId", null)
        userId?.let {
            val monitoringValue = binding.timeInMinutes.text.toString().toInt() * 1000  // Convertir minutos a milisegundos
            val userRef = FirebaseFirestore.getInstance().collection("users").document(it)
            userRef.update("monitoringInSeconds", monitoringValue)
            Toast.makeText(binding.root.context,"Tiempo actualizado!",Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateModeRegisters(mode: String) {
        val userId = context?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)?.getString("userId", null)
        userId?.let {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(it)
            userRef.update("modeRegisters", mode)
        }
    }

    private fun setupEventHandlers() {
        binding.btnSave.setOnClickListener {
            updateUserData()
        }

        binding.btnSaveMonitoring.setOnClickListener {
            updateMonitoringSettings()
        }

        binding.chkManual.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateModeRegisters("m")  // Manual
            }
        }

        binding.chkAuto.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateModeRegisters("a")  // Automatic
            }
        }
        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateNotificationStatus(isChecked)
        }
    }

    private fun updateNotificationStatus(isEnabled: Boolean) {
        val userId = context?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)?.getString("userId", null)
        userId?.let {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(it)
            userRef.update("notificationStatus", isEnabled)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ConfigFragment()
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        // Configuración adicional del mapa, por ejemplo, establecer zoom, etc.
    }

    private fun loadUserData(latitude : Double,longitude : Double) {
        val homeLatLng = LatLng(latitude, longitude)
        map.addMarker(MarkerOptions().position(homeLatLng).title("Dispositivo GLP"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, 15f))
    }

}
