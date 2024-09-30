package com.electric.glp.Activitys

import android.Manifest
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.text.InputType
import android.util.Log
import android.view.Window
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.electric.glp.Adapters.WifiNetworkAdapter
import com.electric.glp.databinding.*
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.zxing.integration.android.IntentIntegrator
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class SyncActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySyncDeviceBinding
    var mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiNetworkAdapter: WifiNetworkAdapter
    private val REQUEST_CODE_PERMISSIONS = 123
    private val REQUEST_CODE_LOCATION = 1234
    private val REQUEST_CHECK_SETTINGS = 0x1
    private var alertDialog: AlertDialog? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var dialogPasswordSSID: Dialog
    private lateinit var dialogSSID: Dialog
    private lateinit var bindingSSID : DialogScanWifiBinding
    private lateinit var bindingPasswordSSID : DialogWifiConnectionBinding

    private lateinit var dialogScanWifi : Dialog
    private lateinit var bindingScanWifi : DialogScanWifiLottieBinding
    private lateinit var dialogConnectDevice : Dialog
    private lateinit var bindingConnectDevice : DialogConectDeviceLottieBinding
    private var internetConnectionDialog: AlertDialog? = null

    private var latitude: Double? = null
    private var longitude: Double? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var type : String = ""
    var resultQr : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding = ActivitySyncDeviceBinding.inflate(layoutInflater)
        bindingSSID = DialogScanWifiBinding.inflate(layoutInflater)
        bindingPasswordSSID = DialogWifiConnectionBinding.inflate(layoutInflater)
        bindingScanWifi = DialogScanWifiLottieBinding.inflate(layoutInflater)
        bindingConnectDevice = DialogConectDeviceLottieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val wifiItemClickListener = WifiItemClickListener()
        wifiNetworkAdapter = WifiNetworkAdapter(mutableListOf(), wifiItemClickListener)

        dialogSSID = Dialog(this)
        dialogSSID.window?.setBackgroundDrawable(ColorDrawable(0))
        dialogSSID.setContentView(bindingSSID.root)
        dialogSSID.setCancelable(false)
        val window: Window = dialogSSID.window!!
        window.setLayout(950, 1400)

        dialogPasswordSSID = Dialog(this)
        dialogPasswordSSID.window?.setBackgroundDrawable(ColorDrawable(0))
        dialogPasswordSSID.setContentView(bindingPasswordSSID.root)
        dialogPasswordSSID.setCancelable(false)
        val window2: Window = dialogPasswordSSID.window!!
        window2.setLayout(950, 950)

        dialogScanWifi = Dialog(this)
        dialogScanWifi.window?.setBackgroundDrawable(ColorDrawable(0))
        dialogScanWifi.setContentView(bindingScanWifi.root)
        dialogScanWifi.setCancelable(false)
        val window3: Window = dialogScanWifi.window!!
        window3.setLayout(950, 950)

        dialogConnectDevice = Dialog(this)
        dialogConnectDevice.window?.setBackgroundDrawable(ColorDrawable(0))
        dialogConnectDevice.setContentView(bindingConnectDevice.root)
        dialogConnectDevice.setCancelable(false)
        val window4: Window = dialogConnectDevice.window!!
        window4.setLayout(950, 950)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        recyclerView = bindingSSID.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = wifiNetworkAdapter
        // Crear una instancia de WifiItemClickListener y pasarla como listener

        binding.btnQr.setOnClickListener {
            if(latitude != null){
                type = "sync"
                if (isWifiEnabled()) {
                    if (!isMobileDataActive()) {
                        //showPermissionExplanationIfNeeded()
                        //scanWifi()
                        initScanner()
                    } else {
                        showAlertDialog("Datos móviles !", "Debes desactivar tus datos móviles")
                    }
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("Wifi !")
                        .setCancelable(false)
                        .setMessage("Se deben activar el WiFi y desactivar los datos móviles.")
                        .setPositiveButton("Ok") { dialog, which ->

                        }
                        .create().show()
                }
            }else{
                Toast.makeText(this,"Activa tu GPS!",Toast.LENGTH_SHORT).show()
            }

        }

        binding.btnOnlyQr.setOnClickListener {
            if(latitude != null){
                type = "qr"
                initScanner()
            }else{
                Toast.makeText(this,"Activa tu GPS!",Toast.LENGTH_SHORT).show()
            }

        }

        showPermissionExplanationIfNeeded()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Obtener la ubicación
            getLocationUpdates()
        } else {
            // Solicitar permisos
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
        }

    }

    private fun getLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    // Guardar latitud y longitud en variables globales
                    latitude = location.latitude
                    longitude = location.longitude

                    // Puedes imprimir los valores o usarlos aquí
                    Log.d("Location Update", "Latitud: $latitude, Longitud: $longitude")
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_LONG).show()
            } else {
                val qr = result.contents.toString()
                resultQr = qr
                if(type == "sync"){
                    connectToWiFi("xFa2.22xP","GLP")
                }else{
                    dialogConnectDevice.show()
                    saveDeviceInDatabase(qr)
                }
                //
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun initScanner() {
        val integrator = IntentIntegrator(this).apply {
            setOrientationLocked(true)
            setPrompt("Enfoque un código QR en el recuadro")
            initiateScan()
        }
    }
    private fun scanWifi() {
        registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()
        dialogScanWifi.show()
        //Toast.makeText(this, "Escaneando redes ...", Toast.LENGTH_SHORT).show()
    }
    private fun saveDeviceInDatabase(qr : String){
        mDatabase.child("device").child(qr).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    saveToFirestore(qr)
                }else{
                    dialogConnectDevice.dismiss()
                    Toast.makeText(this@SyncActivity, "No existe dispositivo!", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun saveDeviceIdInPreferences(deviceId: String) {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString("deviceId", deviceId)
            apply()
        }
    }

    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (ContextCompat.checkSelfPermission(this@SyncActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                runOnUiThread {
                    //dialogScanWifi.dismiss()
                    //cancelDialogScanWifi()
                    dialogSSID.show()
                    val results = wifiManager.scanResults
                    val filteredResults = results
                        .filterNot { it.SSID.isBlank() }
                        .distinctBy { it.SSID }

                    wifiNetworkAdapter.updateData(filteredResults)
                }
            } else {
                cancelDialogScanWifi()
                Toast.makeText(this@SyncActivity, "Error al escanear redes.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cancelDialogScanWifi(){
        dialogScanWifi.dismiss()
    }

    private fun checkIfGpsEnabled() {

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())


        task.addOnSuccessListener {
            //scanWifi()
            getLocationUpdates()
        }

        /*
        task.addOnCompleteListener {
            Toast.makeText(this,"xd2",Toast.LENGTH_SHORT).show()
        }*/

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Toast.makeText(this, "Error al intentar resolver la configuración de ubicación.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun proceedWithFunctionality() {
        checkIfGpsEnabled()
    }

    private fun showPermissionExplanationIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si necesitas mostrar una explicación, puedes usar un AlertDialog aquí
            AlertDialog.Builder(this)
                .setTitle("Se requiere permiso de ubicación")
                .setCancelable(false)
                .setMessage("Debe aceptar todos los permisos de ubicación para sincronizar el dispositivo.")
                .setPositiveButton("Ok") { dialog, which ->
                    checkAndRequestPermissions()
                }
                .create().show()
        } else {
            checkAndRequestPermissions()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    checkIfGpsEnabled()
                    //scanWifi()
                } else {
                    Toast.makeText(this, "Permission denied, can't scan WiFi.", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "Permission denied, can't scan WiFi.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded, REQUEST_CODE_PERMISSIONS)
            //proceedWithFunctionality()
        } else {
            proceedWithFunctionality()
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
    }

    private fun isWifiEnabled(): Boolean {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }
    private fun isMobileDataActive(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else {
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return telephonyManager.dataState == TelephonyManager.DATA_CONNECTED
        }
    }

    private fun showAlertDialog(title: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            if (isMobileDataActive()) {
                showAlertDialog("Conexión de datos móviles", "Tienes conexión de datos móviles activa")
            }else{
                scanWifi()
                dialog.dismiss()
                alertDialog = null
            }
        }
        alertDialog = alertDialogBuilder.create()
        alertDialog?.setOnDismissListener {
            if (!isMobileDataActive()) {
                alertDialog?.dismiss()
                alertDialog = null
            }
        }
        alertDialog?.show()
    }

    private inner class WifiItemClickListener : WifiNetworkAdapter.OnWifiItemClickListener {
        override fun onWifiItemClicked(ssid: String) {
            //val param1 = ssid
            //val param2 = "aukde123#"
            //AQUI MOSTRAR EL DIALOG PARA INTRODUCIR EL PASSWORD
            //sendGETRequest(param1, param2)
            //Toast.makeText(this@MainActivity,ssid,Toast.LENGTH_SHORT).show()
            showWifiConnectDialog(ssid)
        }
    }

    private fun showWifiConnectDialog(ssid: String) {
        // Infla el layout para el diálogo.
        dialogPasswordSSID.show()
        // Configura los elementos del diálogo.
        bindingPasswordSSID.tvWifiName.text = ssid

        // Configura el CheckBox para mostrar u ocultar la contraseña.
        bindingPasswordSSID.cbShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                bindingPasswordSSID.etWifiPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                bindingPasswordSSID.etWifiPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            bindingPasswordSSID.etWifiPassword.setSelection(bindingPasswordSSID.etWifiPassword.text!!.length)
        }

        // Configura el botón de conectar.
        bindingPasswordSSID.btnConnect.setOnClickListener {
            val password = bindingPasswordSSID.etWifiPassword.text.toString()
            dialogPasswordSSID.dismiss() // Cierra el diálogo después de conectar.
            if(password != ""){
                sendGETRequest(ssid,password)
                //dialogPasswordSSID.dismiss()
                //finish()
            }else{
                Toast.makeText(this,"Ingrese una contraseña",Toast.LENGTH_SHORT).show()
            }
        }

        // Configura el botón de cancelar.
        bindingPasswordSSID.btnCancel.setOnClickListener {
            dialogPasswordSSID.dismiss() // Simplemente cierra el diálogo.
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectToWiFi(pin: String, ssid:String) {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as
                    ConnectivityManager
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(pin)
            .setSsidPattern(PatternMatcher(ssid, PatternMatcher.PATTERN_PREFIX))
            .build()
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            //.removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Toast.makeText(this@SyncActivity,"Dispositivo Conectado",Toast.LENGTH_SHORT).show()
                if (!isMobileDataActive()) {
                    scanWifi()
                }else{
                    showAlertDialog("Conexión de datos móviles", "Tienes conexión de datos móviles activa")
                }
            }

            override fun onUnavailable() {
                Toast.makeText(this@SyncActivity,"Error al vincular dispositivo",Toast.LENGTH_SHORT).show()
                super.onUnavailable()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                //finish()
                showInternetConnectionDialog()
                //Toast.makeText(this@SyncDeviceActivity,"Fuera de rango",Toast.LENGTH_SHORT).show()
            }
        }
        connectivityManager.requestNetwork(request, networkCallback)
    }

    fun sendGETRequest(param1: String, param2: String) {
        dialogConnectDevice.show()

        val encodedParam1 = URLEncoder.encode(param1, "UTF-8")
        val encodedParam2 = URLEncoder.encode(param2, "UTF-8")
        val url = "http://192.168.4.1/?param1=$encodedParam1&param2=$encodedParam2"
        Log.d("SyncActivity", "URL generada: $url")


        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                Log.d("SyncActivity", "Respuesta exitosa: ${response.body?.string()}")
                runOnUiThread {
                    Toast.makeText(this@SyncActivity, "Respuesta exitosa", Toast.LENGTH_SHORT).show()
                }
                dialogConnectDevice.dismiss()
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("SyncActivity", "Error en la solicitud: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@SyncActivity, "Error en la solicitud: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                dialogConnectDevice.dismiss()
            }
        })

    }

    private fun showInternetConnectionDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Conexión a Internet")
        alertDialogBuilder.setMessage("Por favor, conéctese a internet para continuar.")
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setPositiveButton("Ok") { dialog, which ->
            // Llama a la función checkInternetConnection para verificar la conexión
            checkInternetConnection()
        }

        runOnUiThread {
            internetConnectionDialog = alertDialogBuilder.create()
            internetConnectionDialog?.show()
        }

    }

    private fun checkInternetConnection() {
        if (isConnectedToInternet()) {
            // Si hay conexión a Internet, cierra el diálogo y finaliza la actividad
            internetConnectionDialog?.dismiss()
            saveDeviceInDatabase(resultQr)
        } else {
            // Si no hay conexión a Internet, muestra el diálogo de conexión nuevamente
            showInternetConnectionDialog()
        }
    }

    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun saveToFirestore(scannedDeviceId: String) {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", "")

        val firestore = FirebaseFirestore.getInstance()
        val userDocRef = firestore.collection("users").document(userId!!)

        val dataUpdated = mapOf(
            "lat" to latitude,
            "lon" to longitude,
            "deviceId" to scannedDeviceId
        )

        userDocRef.update(dataUpdated).addOnSuccessListener {
            userDocRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Extraer los valores de monitoringInSeconds y modeRegisters
                    val monitoringInSeconds = documentSnapshot.getLong("monitoringInSeconds") ?: 0L
                    val modeRegisters = documentSnapshot.getString("modeRegisters") ?: ""

                    // Ahora guarda estos valores en Firebase Realtime Database
                    val database = FirebaseDatabase.getInstance()
                    val deviceRef = database.getReference("device/$scannedDeviceId")

                    // Crear un mapa para almacenar los valores en Realtime Database
                    val dataMap = mapOf(
                        "monitoringInSeconds" to monitoringInSeconds,
                        "modeRegisters" to modeRegisters
                    )
                    // Actualizar los valores en Realtime Database
                    deviceRef.updateChildren(dataMap).addOnSuccessListener {
                        // Una vez que se complete la escritura en Realtime Database
                        saveDeviceIdInPreferences(scannedDeviceId)
                        startActivity(Intent(this, ActivityMenu::class.java))
                        finish()
                    }.addOnFailureListener {
                        // Maneja cualquier error al escribir en Realtime Database
                        Log.e("FirestoreError", "Error al escribir en Realtime Database", it)
                    }
                } else {
                    Log.e("FirestoreError", "Documento no encontrado")
                }
            }.addOnFailureListener {
                // Maneja cualquier error al leer de Firestore
                Log.e("FirestoreError", "Error al obtener documento de Firestore", it)
            }
        }



    }


    private fun getUserDetailsFromPreferences(): Pair<String?, String?> {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", "")   // Retorna null si no existe la clave "userId"
        val deviceId = prefs.getString("deviceId", "") // Retorna null si no existe la clave "deviceId"
        return Pair(userId, deviceId)
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

}