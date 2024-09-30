package com.electric.glp.Fragments.Menu

import RegistersAdapter
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.electric.glp.Models.RegisterData
import com.electric.glp.databinding.FragmentRegistersBinding
import com.google.firebase.database.*

class RegistersFragment : Fragment() {
    private var _binding: FragmentRegistersBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var adapter: RegistersAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegistersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null) ?: return
        database = FirebaseDatabase.getInstance().getReference("device/$deviceId/registers")
        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = RegistersAdapter(listOf(),database)
        binding.recyclerView.adapter = adapter
    }

    private fun loadData() {
        // Asumiendo que 'database' ya está inicializada correctamente apuntando a 'registers'
        database.orderByChild("timestamp").limitToLast(20).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val registers = mutableListOf<RegisterData>()
                    snapshot.children.forEach { child ->
                        val glp = child.child("glp").getValue(Int::class.java) ?: 0
                        val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0
                        val co = child.child("co").getValue(Int::class.java) ?: 0
                        val smoke = child.child("smoke").getValue(Int::class.java) ?: 0
                        val key = child.key ?: ""
                        registers.add(RegisterData(glp, timestamp, co, smoke, key))
                    }
                    // Invertimos la lista para tener los registros del más reciente al más antiguo
                    registers.reverse()
                    adapter.updateData(registers)
                    binding.linearFragment2.visibility = View.VISIBLE
                    binding.linearLottie2.visibility = View.GONE
                    binding.linearEmpty.visibility = View.GONE
                }else{
                    binding.linearFragment2.visibility = View.GONE
                    binding.linearLottie2.visibility = View.GONE
                    binding.linearEmpty.visibility = View.VISIBLE
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
                println("Failed to read data: ${error.message}")
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = RegistersFragment()
    }
}
