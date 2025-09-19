package com.bugbd.pdfprinter.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bugbd.pdfprinter.databinding.FragmentScansDataBinding
import com.bugbd.pdfprinter.local_bd.PreferenceManager
import com.bugbd.pdfprinter.local_bd.ScannerDB

class ScansDataFragment : Fragment() {
    private lateinit var binding: FragmentScansDataBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var scannerDB: ScannerDB
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentScansDataBinding.inflate(layoutInflater)
        preferenceManager = PreferenceManager(requireContext())
        scannerDB = ScannerDB.getInstance(requireContext())

        clickEvent()

        return binding.root
    }

    private fun clickEvent() {

    }
}