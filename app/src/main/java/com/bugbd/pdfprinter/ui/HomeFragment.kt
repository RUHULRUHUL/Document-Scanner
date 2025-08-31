package com.bugbd.pdfprinter.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bugbd.pdfprinter.R
import com.bugbd.pdfprinter.adapter.BrowsePdf
import com.bugbd.pdfprinter.adapter.LanguageAdapter
import com.bugbd.pdfprinter.adapter.OthersAdapter
import com.bugbd.pdfprinter.adapter.PdfAdapter
import com.bugbd.pdfprinter.databinding.FragmentHomeBinding
import com.bugbd.pdfprinter.databinding.FragmentSettingsBinding
import com.bugbd.pdfprinter.ext.setCheckedRadio
import com.bugbd.pdfprinter.ext.setDarkLightThem
import com.bugbd.pdfprinter.ext.setLocale
import com.bugbd.pdfprinter.helper.PdfFile
import com.bugbd.pdfprinter.helper.PdfFileItem
import com.bugbd.pdfprinter.helper.Utils
import com.bugbd.pdfprinter.helper.Utils.Companion.getAllPdfUris
import com.bugbd.pdfprinter.helper.Utils.Companion.getPdfFiles
import com.bugbd.pdfprinter.helper.Utils.Companion.getPdfFilesFromDownloadFolder
import com.bugbd.pdfprinter.helper.Utils.Companion.getPdfFilesFromDownloads
import com.bugbd.pdfprinter.helper.drawerList
import com.bugbd.pdfprinter.helper.getAllMediaFiles
import com.bugbd.pdfprinter.helper.getPdfs
import com.bugbd.pdfprinter.helper.logD
import com.bugbd.pdfprinter.helper.requestAllFilesAccessPermission
import com.bugbd.pdfprinter.local_bd.PreferenceManager
import com.bugbd.pdfprinter.local_bd.ScannerDB
import com.bugbd.qrcode.model.LanguageItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var pdfAdapter: PdfAdapter
    private lateinit var scannerDB: ScannerDB
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(layoutInflater)
        preferenceManager = PreferenceManager(requireContext())
        scannerDB = ScannerDB.getInstance(requireContext())
        initView()
        clickEvent()
        pdfAdapter()
        return binding.root
    }

    private fun clickEvent() {

    }

    @SuppressLint("SetTextI18n")
    private fun initView() {

    }

    private fun pdfAdapter() {
        binding.pdfRV.layoutManager =
            LinearLayoutManager(requireContext())
        binding.pdfRV.setHasFixedSize(true)
        pdfAdapter = PdfAdapter(requireContext()) {
            Utils.shareFile(requireContext(), it.fileName, it.fileUrl)
        }
        binding.pdfRV.adapter = pdfAdapter
        scannerDB.scannerDao().getScanFileList()
            .observe(requireActivity()) {
                if (it.isNotEmpty()) {
                    pdfAdapter.addItems(it)
                } else {
                }
            }

    }

}