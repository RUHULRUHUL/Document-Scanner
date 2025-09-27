package com.bugbd.pdfprinter.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bugbd.pdfprinter.R
import com.bugbd.pdfprinter.adapter.QRScanAdapter
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

        if (isAdded){
            binding.appbar.tvTitle.text = requireContext().getString(R.string.qr_scans)
        }

        getScanResult()
        clickEvent()

        return binding.root
    }

    private fun getScanResult() {
        try {
            scannerDB.scannerDao().getAllScanHistory()
                .observe(viewLifecycleOwner) {
                    val adapter = QRScanAdapter(it, context = requireContext())
                    binding.pdfRV.layoutManager =
                        LinearLayoutManager(requireContext())
                    binding.pdfRV.setHasFixedSize(true)
                    binding.pdfRV.adapter = adapter
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clickEvent() {

    }
}