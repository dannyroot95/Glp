package com.electric.glp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.electric.glp.databinding.FragmentGeneralBinding
import com.google.firebase.database.*

class GeneralFragment : Fragment() {
    private var _binding: FragmentGeneralBinding? = null
    private val binding get() = _binding!!

    private var database: DatabaseReference = FirebaseDatabase.getInstance().getReference("device")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneralBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getParametersSensor(database)
    }

    private fun getParametersSensor(database: DatabaseReference) {
        val prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("deviceId", null) ?: return
        database.child(deviceId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val glpValue = snapshot.child("glp").getValue(Int::class.java)
                binding.glpValue.text = glpValue.toString()+"Bq/m"
                binding.linearFragment1.visibility = View.VISIBLE
                binding.linearLottie.visibility = View.GONE
            }
            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value: ${error.message}")
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = GeneralFragment()
    }
}
