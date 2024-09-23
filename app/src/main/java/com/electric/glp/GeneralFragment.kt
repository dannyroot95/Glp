import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.electric.glp.databinding.FragmentGeneralBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.*

class GeneralFragment : Fragment() {
    private var _binding: FragmentGeneralBinding? = null
    private val binding get() = _binding!!

    private var database: DatabaseReference = FirebaseDatabase.getInstance().getReference("device")
    private val dataEntries = ArrayList<Entry>() // Lista para almacenar los valores del gráfico

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneralBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLineChart()
        getParametersSensor(database)
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
        val prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null) ?: return
        database.child(deviceId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val glpValue = snapshot.child("glp").getValue(Int::class.java) ?: 0
                binding.glpValue.text = "$glpValue Bq/m"

                // Agregar el valor al gráfico
                val entry = Entry(dataEntries.size.toFloat(), glpValue.toFloat()) // x = índice, y = valor glp
                dataEntries.add(entry)

                // Actualizar el gráfico
                updateLineChart()

                binding.linearFragment1.visibility = View.VISIBLE
                binding.linearLottie.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value: ${error.message}")
            }
        })
    }

    private fun updateLineChart() {
        val lineDataSet = LineDataSet(dataEntries, "GLP Values")
        lineDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList() // Colores predefinidos
        lineDataSet.setDrawValues(false)
        lineDataSet.setDrawCircles(true)

        val lineData = LineData(lineDataSet)
        binding.lineChart.data = lineData
        binding.lineChart.invalidate() // Redibujar el gráfico
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = GeneralFragment()
    }
}
