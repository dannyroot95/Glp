package com.electric.glp.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.electric.glp.Fragments.Sliders.FirstFragment
import com.electric.glp.Fragments.Sliders.SecondFragment
import com.electric.glp.Fragments.Sliders.ThirdFragment


class MyPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3 // Número de fragmentos

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FirstFragment()
            1 -> SecondFragment()
            2 -> ThirdFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}
