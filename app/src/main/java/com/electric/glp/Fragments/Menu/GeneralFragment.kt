package com.electric.glp.Fragments.Menu

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.electric.glp.Utils.ConvertTimestampToDateTime
import com.electric.glp.databinding.DialogDatetimePickerBinding
import com.electric.glp.databinding.FragmentGeneralBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.database.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale
import kotlin.collections.ArrayList

class GeneralFragment : Fragment() {
    private var _binding: FragmentGeneralBinding? = null
    private val binding get() = _binding!!

    private var database: DatabaseReference = FirebaseDatabase.getInstance().getReference("device")
    private var currentListener: ValueEventListener? = null
    private val dataEntries = ArrayList<Entry>()
    private var onRegister : String = "off"
    private val sensorDataList = mutableListOf<Map<String, Any>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneralBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Permiso concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
            return
        }

        listenerModeRegister()
        setupLineChart()
        getParametersSensorFilter(database, "last24Hours")
        updateChipSelection(binding.chipLastHour, binding.chipNow,binding.chipMonth,binding.chipYear,binding.chipCustom, Color.WHITE, Color.BLACK)

        binding.chipLastHour.setOnClickListener {
            binding.linearFragment1.visibility = View.GONE
            binding.linearLottie.visibility = View.VISIBLE
            onRegister = "off"
            updateChipSelection(binding.chipLastHour, binding.chipNow,binding.chipMonth,binding.chipYear,binding.chipCustom, Color.WHITE, Color.BLACK)
            getParametersSensorFilter(database, "last24Hours")
        }

        binding.chipNow.setOnClickListener {
            binding.linearFragment1.visibility = View.GONE
            binding.linearLottie.visibility = View.VISIBLE
            onRegister = "on"
            updateChipSelection(binding.chipNow, binding.chipLastHour,binding.chipMonth,binding.chipYear,binding.chipCustom, Color.WHITE, Color.BLACK)
            getParametersSensor(database)
        }

        binding.chipMonth.setOnClickListener {
            binding.linearFragment1.visibility = View.GONE
            binding.linearLottie.visibility = View.VISIBLE
            onRegister = "off"
            updateChipSelection(binding.chipMonth,binding.chipNow,binding.chipLastHour,binding.chipYear,binding.chipCustom, Color.WHITE, Color.BLACK)
            getParametersSensorFilter(database, "lastMonth")
        }

        binding.chipYear.setOnClickListener {
            binding.linearFragment1.visibility = View.GONE
            binding.linearLottie.visibility = View.VISIBLE
            onRegister = "off"
            updateChipSelection(binding.chipYear,binding.chipMonth,binding.chipNow,binding.chipLastHour,binding.chipCustom, Color.WHITE, Color.BLACK)
            getParametersSensorFilter(database, "lastYear")
        }

        binding.chipCustom.setOnClickListener {
            showDateTimePickerDialog()
            onRegister = "off"
            updateChipSelection(binding.chipCustom,binding.chipMonth,binding.chipNow,binding.chipLastHour,binding.chipYear, Color.WHITE, Color.BLACK)
        }

        binding.btnSaveRegister.setOnClickListener {
            saveRegisterToDatabase()
        }

        binding.btnDownloadData.setOnClickListener {
            if (sensorDataList.isEmpty()) {
                Toast.makeText(requireContext(), "No hay datos para descargar", Toast.LENGTH_SHORT).show()
            } else {
                generateAndOpenPdf()
            }
        }
  }

    private fun updateChipSelection(selectedChip: View, deselectedChip: View,deselectedChip2: View,deselectedChip3: View,deselectedChip4: View, selectedTextColor: Int, deselectedTextColor: Int) {
        // Actualiza el chip seleccionado
        selectedChip.background.setTint(Color.rgb(4, 156, 4))
        (selectedChip as? android.widget.TextView)?.setTextColor(selectedTextColor)

        // Actualiza el chip no seleccionado
        deselectedChip.background.setTint(Color.rgb(237, 236, 236))
        (deselectedChip as? android.widget.TextView)?.setTextColor(deselectedTextColor)

        // Actualiza el chip no seleccionado
        deselectedChip2.background.setTint(Color.rgb(237, 236, 236))
        (deselectedChip2 as? android.widget.TextView)?.setTextColor(deselectedTextColor)

        // Actualiza el chip no seleccionado
        deselectedChip3.background.setTint(Color.rgb(237, 236, 236))
        (deselectedChip3 as? android.widget.TextView)?.setTextColor(deselectedTextColor)

        deselectedChip4.background.setTint(Color.rgb(237, 236, 236))
        (deselectedChip4 as? android.widget.TextView)?.setTextColor(deselectedTextColor)

    }

    private fun setupLineChart() {
        val lineChart = binding.lineChart
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)

        // Configurar eje X
        val xAxis: XAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        // Desactivar el eje derecho
        lineChart.axisRight.isEnabled = false
    }

    private fun getParametersSensor(database: DatabaseReference) {
        removeCurrentListener()  // Asegúrate de remover cualquier listener antiguo
        sensorDataList.clear()

        val prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null) ?: return
        dataEntries.clear()
        val timestampPath = "$deviceId/timestamp"

        currentListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val timeValue = snapshot.getValue(Long::class.java) ?: 0

                // Consulta para obtener el valor de GLP cuando el timestamp cambia
                database.child(deviceId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val glpValue = dataSnapshot.child("glp").getValue(Int::class.java) ?: 0
                        val coValue = dataSnapshot.child("co").getValue(Int::class.java) ?: 0
                        val smokeValue = dataSnapshot.child("smoke").getValue(Int::class.java) ?: 0
                        binding.coValue.text = "$coValue"
                        binding.smokeValue.text = "$smokeValue"
                        binding.glpValue.text = "$glpValue"
                        binding.lastUpdateTime.text = "Última actualización: " + ConvertTimestampToDateTime().convert(timeValue)

                        val newData = mapOf(
                            "co" to coValue,
                            "glp" to glpValue,
                            "smoke" to smokeValue,
                            "timestamp" to ConvertTimestampToDateTime().convert(timeValue)
                        )
                        sensorDataList.add(newData)

                        // Agregar el valor al gráfico
                        val entry = Entry(dataEntries.size.toFloat(), glpValue.toFloat()) // x = índice, y = valor glp
                        dataEntries.add(entry)

                        // Actualizar el gráfico
                        updateLineChart()

                        binding.linearFragment1.visibility = View.VISIBLE
                        binding.linearLottie.visibility = View.GONE
                    }

                    override fun onCancelled(dbError: DatabaseError) {
                        println("Failed to read glp value: ${dbError.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to read timestamp: ${error.message}")
            }
        }

        database.child(timestampPath).addValueEventListener(currentListener!!)
    }


    private fun updateLineChart() {
        val lineDataSet = LineDataSet(dataEntries, "Concentración en ppm")

        // Colores predefinidos para la línea
        lineDataSet.color = Color.rgb(0, 144, 7)
        lineDataSet.setDrawCircles(true)
        lineDataSet.circleRadius = 4f
        lineDataSet.setCircleColor(Color.rgb(0, 144, 7))

        // Hacer la línea curva
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        // Mostrar los valores encima de los círculos
        lineDataSet.setDrawValues(true)
        lineDataSet.valueTextSize = 12f
        lineDataSet.valueTextColor = Color.BLACK

        // Rellenar debajo de la línea
        lineDataSet.setDrawFilled(true)
        val drawable = ContextCompat.getDrawable(binding.root.context, com.electric.glp.R.drawable.green_gradient)
        lineDataSet.fillDrawable = drawable

        // Actualizar el gráfico
        val lineData = LineData(lineDataSet)
        binding.lineChart.data = lineData
        binding.lineChart.invalidate()
    }

    private fun getParametersSensorFilter(database: DatabaseReference, filterType: String) {
        removeCurrentListener()  // Remover el listener actual antes de añadir uno nuevo
        sensorDataList.clear()

        val startTime = getStartTimestamp(filterType) / 1000
        val endTime = getEndTimestamp(filterType) / 1000

        val prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null) ?: return

        val query = database.child(deviceId).child("registers").orderByChild("timestamp").startAt(startTime.toDouble())
        var ctx = 0
        currentListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    dataEntries.clear()
                    snapshot.children.forEach { child ->
                        val glpValue = child.child("glp").getValue(Int::class.java) ?: 0
                        val coValue = child.child("co").getValue(Int::class.java) ?: 0
                        val smokeValue = child.child("smoke").getValue(Int::class.java) ?: 0
                        val timeValue = child.child("timestamp").getValue(Long::class.java) ?: 0

                        binding.glpValue.text = "$glpValue"
                        binding.coValue.text = "$coValue"
                        binding.smokeValue.text = "$smokeValue"
                        binding.lastUpdateTime.text = "Ultima actualizacion : "+ConvertTimestampToDateTime().convert(timeValue)

                        if(timeValue in startTime..endTime){
                            val newData = mapOf(
                                "co" to coValue,
                                "glp" to glpValue,
                                "smoke" to smokeValue,
                                "timestamp" to ConvertTimestampToDateTime().convert(timeValue)
                            )
                            sensorDataList.add(newData)
                            val index = (ctx++).toFloat()
                            val entry = Entry(index, glpValue.toFloat())
                            dataEntries.add(entry)
                        }
                    }
                    updateLineChart()
                    binding.linearFragment1.visibility = View.VISIBLE
                    binding.linearLottie.visibility = View.GONE
                }else{
                    binding.linearFragment1.visibility = View.GONE
                    binding.linearLottie.visibility = View.VISIBLE
                    onRegister = "on"
                    updateChipSelection(binding.chipNow, binding.chipLastHour,binding.chipMonth,binding.chipYear,binding.chipCustom, Color.WHITE, Color.BLACK)
                    getParametersSensor(database)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value: ${error.message}")
            }
        }

        query.addListenerForSingleValueEvent(currentListener!!)
    }

    private fun getParametersSensorFilterCustom(database: DatabaseReference, d1: Long,d2:Long) {
        removeCurrentListener()  // Remover el listener actual antes de añadir uno nuevo
        sensorDataList.clear()

        val startTime = d1
        val endTime = d2

        val prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null) ?: return

        val query = database.child(deviceId).child("registers").orderByChild("timestamp").startAt(startTime.toDouble())
        var ctx = 0
        currentListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    dataEntries.clear()
                    snapshot.children.forEach { child ->
                        val glpValue = child.child("glp").getValue(Int::class.java) ?: 0
                        val coValue = child.child("co").getValue(Int::class.java) ?: 0
                        val smokeValue = child.child("smoke").getValue(Int::class.java) ?: 0
                        val timeValue = child.child("timestamp").getValue(Long::class.java) ?: 0

                        binding.glpValue.text = "$glpValue"
                        binding.coValue.text = "$coValue"
                        binding.smokeValue.text = "$smokeValue"
                        binding.lastUpdateTime.text = "Ultima actualizacion : "+ConvertTimestampToDateTime().convert(timeValue)

                        if(timeValue in startTime..endTime){
                            val newData = mapOf(
                                "co" to coValue,
                                "glp" to glpValue,
                                "smoke" to smokeValue,
                                "timestamp" to ConvertTimestampToDateTime().convert(timeValue)
                            )
                            sensorDataList.add(newData)
                            val index = (ctx++).toFloat()
                            val entry = Entry(index, glpValue.toFloat())
                            dataEntries.add(entry)
                        }
                    }
                    updateLineChart()
                    binding.linearFragment1.visibility = View.VISIBLE
                    binding.linearLottie.visibility = View.GONE
                }else{
                    binding.linearFragment1.visibility = View.GONE
                    binding.linearLottie.visibility = View.VISIBLE
                    onRegister = "on"
                    updateChipSelection(binding.chipNow, binding.chipLastHour,binding.chipMonth,binding.chipYear,binding.chipCustom, Color.WHITE, Color.BLACK)
                    getParametersSensor(database)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value: ${error.message}")
            }
        }

        query.addListenerForSingleValueEvent(currentListener!!)
    }

    private fun getStartTimestamp(filterType: String): Long {
        val currentTime = System.currentTimeMillis() / 1000  // Convierte milisegundos a segundos
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()
        val startOfDay = LocalDate.now().atStartOfDay(zoneId).toInstant().toEpochMilli()
        val firstDayOfMonth = today.withDayOfMonth(1)
        val startOfMonth = (firstDayOfMonth.atStartOfDay(zoneId).toInstant().epochSecond)*1000
        val firstDayOfYear = today.withDayOfYear(1)
        val startOfYear = (firstDayOfYear.atStartOfDay(zoneId).toInstant().epochSecond)*1000
        return when (filterType) {
            "last24Hours" -> startOfDay
            "lastMonth" -> startOfMonth
            "lastYear" -> startOfYear
            else -> currentTime - 3600
        }
    }

    private fun getEndTimestamp(filterType: String): Long {
        val currentTime = System.currentTimeMillis() / 1000  // Convierte milisegundos a segundos
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()
        val endOfDay = LocalDate.now().atTime(23, 59, 59, 999_999_999).atZone(zoneId).toInstant().toEpochMilli()
        val lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth())
        val endOfMonth = (lastDayOfMonth.atTime(23, 59, 59).atZone(zoneId).toInstant().epochSecond)*1000
        val lastDayOfYear = today.withDayOfYear(today.lengthOfYear())
        val endOfYear = (lastDayOfYear.atTime(23, 59, 59).atZone(zoneId).toInstant().epochSecond)*1000
        return when (filterType) {
            "last24Hours" -> endOfDay
            "lastMonth" -> endOfMonth
            "lastYear" -> endOfYear
            else -> currentTime - 3600
        }
    }

    private fun removeCurrentListener() {
        val prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null)
        if (deviceId != null && currentListener != null) {
            // Suponiendo que podrías haber añadido el listener a diferentes nodos
            val timestampPath = "$deviceId/timestamp"
            val registersPath = "$deviceId/registers"

            // Remover el listener de ambos posibles nodos
            database.child(timestampPath).removeEventListener(currentListener!!)
            database.child(registersPath).removeEventListener(currentListener!!)

            currentListener = null
        }
    }

    private fun generateAndOpenPdf() {
        if (sensorDataList.isEmpty()) {
            Toast.makeText(requireContext(), "No hay datos para generar el PDF", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val recordsPerPage = 7
        var yPosition = 50f
        var recordCount = 0
        var pageNumber = 1 // Inicia en la página 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        paint.textSize = 14f
        paint.color = android.graphics.Color.BLACK

        // Título de la primera página
        paint.textSize = 18f
        canvas.drawText("Reporte de Datos del Sensor - Página $pageNumber", 150f, yPosition, paint)
        yPosition += 30f
        paint.textSize = 14f

        sensorDataList.forEachIndexed { index, data ->
            if (recordCount == recordsPerPage) {
                pdfDocument.finishPage(page)
                pageNumber++ // Incrementar el número de página
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                canvas.drawText("Reporte de Datos del Sensor - Página $pageNumber", 150f, 50f, paint)
                yPosition = 80f // Reiniciar la posición Y para la nueva página
                recordCount = 0 // Reiniciar el contador de registros
            }

            canvas.drawText("Registro ${index + 1}:", 50f, yPosition, paint)
            yPosition += 20f
            canvas.drawText("CO: ${data["co"]}"+" ppm", 50f, yPosition, paint)
            yPosition += 20f
            canvas.drawText("GLP: ${data["glp"]}"+" ppm/glp", 50f, yPosition, paint)
            yPosition += 20f
            canvas.drawText("Humo: ${data["smoke"]}"+" ppm", 50f, yPosition, paint)
            yPosition += 20f
            canvas.drawText("Fecha y hora: ${data["timestamp"]}", 50f, yPosition, paint)
            yPosition += 30f

            recordCount++
        }

        pdfDocument.finishPage(page)

        // Ruta del archivo PDF
        val file = File(requireContext().getExternalFilesDir(null), "Reporte_Sensor.pdf")

        try {
            // Verificar si el archivo ya existe
            if (file.exists()) {
                file.delete() // Eliminar el archivo existente para reemplazarlo
            }

            // Guardar el nuevo archivo
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            //Toast.makeText(requireContext(), "PDF guardado correctamente", Toast.LENGTH_SHORT).show()

            // Abrir el PDF
            openPdf(file)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al guardar el PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openPdf(file: File) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "No se encontró una app para abrir PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveRegisterToDatabase() {

        if(onRegister == "on"){
            val prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val deviceId = prefs.getString("deviceId", null)
            val glpValue = binding.glpValue.text.toString().toInt()
            val coValue = binding.coValue.text.toString().toInt()
            val smokeValue = binding.smokeValue.text.toString().toInt()
            val timestamp = System.currentTimeMillis() / 1000

            if (deviceId != null && timestamp != 0L) {
                val databaseRef = database.child(deviceId).child("registers").push()  // Crea un nuevo nodo en 'registers'
                val registerData = mapOf(
                    "glp" to glpValue,
                    "timestamp" to timestamp,
                    "co" to coValue,
                    "smoke" to smokeValue
                )

                databaseRef.setValue(registerData)
                Toast.makeText(context, "Registro guardado", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(context, "No hay datos para guardar.", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(context, "No puede guardar este dato.", Toast.LENGTH_SHORT).show()
        }


    }

    private fun listenerModeRegister(){
        val prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null)

        if (deviceId != null) {
            database.child(deviceId).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val modeRegisters = snapshot.child("modeRegisters").getValue(String::class.java)
                    if (modeRegisters == "m") {
                        binding.btnSaveRegister.visibility = View.VISIBLE
                    } else {
                        binding.btnSaveRegister.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    private fun showDateTimePickerDialog() {
        val bindingx = DialogDatetimePickerBinding.inflate(LayoutInflater.from(requireContext()))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(bindingx.root)
            .setTitle("Seleccionar Fechas")
            .setNegativeButton("Cancelar") { _, _ ->
                binding.linearFragment1.visibility = View.GONE
                binding.linearLottie.visibility = View.VISIBLE
                onRegister = "off"
                updateChipSelection(binding.chipLastHour, binding.chipNow,binding.chipMonth,binding.chipYear,binding.chipCustom, Color.WHITE, Color.BLACK)
                getParametersSensorFilter(database, "last24Hours")
                Toast.makeText(requireContext(), "Cancelado", Toast.LENGTH_SHORT).show()
            }
            .create()

        bindingx.etStartDate.setOnClickListener { showDateTimePicker(bindingx.etStartDate) }
        bindingx.etEndDate.setOnClickListener { showDateTimePicker(bindingx.etEndDate) }

        bindingx.btnAccept.setOnClickListener {

            val startTimestamp = convertToTimestamp(bindingx.etStartDate.text.toString())
            val endTimestamp = convertToTimestamp(bindingx.etEndDate.text.toString())

            if (startTimestamp != null && endTimestamp != null) {
                if(endTimestamp > startTimestamp){
                    binding.linearFragment1.visibility = View.GONE
                    binding.linearLottie.visibility = View.VISIBLE
                    getParametersSensorFilterCustom(database, startTimestamp,endTimestamp)
                    dialog.dismiss()
                }else{
                    getParametersSensorFilter(database, "last24Hours")
                    Toast.makeText(
                        requireContext(),
                        "La fecha inicial debe ser menor a la fecha final",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }else{
                Toast.makeText(
                    requireContext(),
                    "Complete los campos",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        dialog.show()
    }

    private fun showDateTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val timePicker = TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        val selectedDate = Calendar.getInstance()
                        selectedDate.set(year, month, dayOfMonth, hourOfDay, minute)
                        editText.setText(dateFormat.format(selectedDate.time))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun convertToTimestamp(dateTime: String): Long? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            format.parse(dateTime)?.time?.div(1000)  // Convertir a segundos (Firebase usa epoch seconds)
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeCurrentListener()  // Remover el listener cuando se destruye la vista
        _binding = null
    }

    companion object {
        fun newInstance() = GeneralFragment()
    }
}
