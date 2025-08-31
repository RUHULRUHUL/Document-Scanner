package com.bugbd.pdfprinter.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bugbd.pdfprinter.ui.HomeFragment
import com.bugbd.pdfprinter.ui.SettingsFragment

class MyQRTab(fragment: FragmentActivity) :
    FragmentStateAdapter(fragment){
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 ->     HomeFragment()
            1 ->     SettingsFragment()
            //2 ->     MyQRCodeFragment()
            else -> HomeFragment()
        }
    }

}