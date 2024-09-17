package com.electric.glp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.electric.glp.databinding.FragmentThirdBinding


class ThirdFragment : Fragment() {

    private var _binding: FragmentThirdBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentThirdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonGoToNextActivity.setOnClickListener {
            // Inicia la nueva actividad
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)

            // Guarda una preferencia para evitar que ViewPager2 se muestre en el futuro
            val prefs = activity?.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
            prefs?.edit()?.putBoolean("onboarding_completed", true)?.apply()

            // Finaliza la actividad actual
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}