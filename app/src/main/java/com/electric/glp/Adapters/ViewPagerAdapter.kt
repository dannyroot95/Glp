package com.electric.glp.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.electric.glp.Fragments.Menu.ConfigFragment
import com.electric.glp.Fragments.Menu.GeneralFragment
import com.electric.glp.Fragments.Menu.RegistersFragment

class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> GeneralFragment.newInstance()
            1 -> RegistersFragment.newInstance()
            2 -> ConfigFragment.newInstance()
            else -> Fragment()
        }
    }
}
