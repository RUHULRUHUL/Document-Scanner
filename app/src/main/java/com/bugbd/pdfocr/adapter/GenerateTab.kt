package com.bugbd.pdfocr.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bugbd.pdfocr.ui.HomeFragment
import com.bugbd.pdfocr.ui.SettingsFragment

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