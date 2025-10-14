package com.bugbd.pdfprinter.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bugbd.pdfprinter.R
import com.bugbd.pdfprinter.ScanDetailsActivity
import com.bugbd.pdfprinter.adapter.QRScanAdapter
import com.bugbd.pdfprinter.databinding.FragmentScansDataBinding
import com.bugbd.pdfprinter.helper.Utils
import com.bugbd.pdfprinter.helper.getBarCodeFormat
import com.bugbd.pdfprinter.helper.getBarcodeResult
import com.bugbd.pdfprinter.local_bd.PreferenceManager
import com.bugbd.pdfprinter.local_bd.ScannerDB
import com.bugbd.pdfprinter.model.ScanHistory
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import kotlinx.coroutines.launch

class ScansDataFragment : Fragment() {
    private lateinit var binding: FragmentScansDataBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var scannerDB: ScannerDB
    val barCodeOptions  = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_AZTEC)
        .enableAutoZoom()
        .build()
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
                    if (it.isNullOrEmpty()){
                        binding.emptyStateInclude.root.visibility = View.VISIBLE
                        binding.pdfRV.visibility = View.GONE
                    }else{
                        binding.emptyStateInclude.root.visibility = View.GONE
                        binding.pdfRV.visibility = View.VISIBLE
                        val adapter = QRScanAdapter(it, context = requireContext())
                        binding.pdfRV.layoutManager =
                            LinearLayoutManager(requireContext())
                        binding.pdfRV.setHasFixedSize(true)
                        binding.pdfRV.adapter = adapter
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clickEvent() {
        binding.emptyStateInclude.actionButton.setOnClickListener {
            openCamera()
        }

    }

    private fun openCamera(){
        val barCodeScanner = GmsBarcodeScanning.getClient(requireContext(), barCodeOptions)
        barCodeScanner.startScan()
            .addOnSuccessListener { barcode ->
                lifecycleScope.launch {
                    val formatType = getBarCodeFormat(type = barcode.valueType,barcode)
                    val scanData = getBarcodeResult(type = barcode.valueType,barcode)
                    val scanHistory = ScanHistory(0, formatType, scanData, Utils.getCurrentTimeMills())
                    scannerDB.scannerDao().insertScanHistory(scanHistory)
                    val intent = Intent(requireContext(), ScanDetailsActivity::class.java)
                    intent.putExtra("scanned_text", scanData)
                    startActivity(intent)
                    Log.d("barcodes", "Extracted: $scanData")
                }
            }
            .addOnCanceledListener {
                // Task canceled
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
            }
    }
}