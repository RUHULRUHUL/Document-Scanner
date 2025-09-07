package com.bugbd.pdfprinter.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
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
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bugbd.pdfprinter.LanguageSelectedActivity
import com.bugbd.pdfprinter.MainActivity
import com.bugbd.pdfprinter.adapter.ScanAdapter
import com.bugbd.pdfprinter.bottom_sheet.MyBottomSheetFragment
import com.bugbd.qrcode.model.ScanFile
import com.bugbd.qrcode.model.scanItems
import com.bugbd.qrcode.model.supportedLanguagesV2
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var pdfAdapter: PdfAdapter
    private lateinit var scannerDB: ScannerDB

    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var options: GmsDocumentScannerOptions.Builder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(layoutInflater)
        preferenceManager = PreferenceManager(requireContext())
        scannerDB = ScannerDB.getInstance(requireContext())
        initView()
        clickEvent()
        scanAdapter()
        pdfAdapter()
        return binding.root
    }

    private fun clickEvent() {

    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        scannerLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                documentHandleActivityResult(result)
            }
        try {
            options = GmsDocumentScannerOptions.Builder()
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE_WITH_FILTER)
                .setResultFormats(
                    GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
                )
                .setGalleryImportAllowed(true)
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                .setPageLimit(1)
        } catch (e: Exception) {
            Utils.showToast(requireContext(), e.localizedMessage ?: "Something went wrong ")
            e.printStackTrace()
        }

    }

    private fun openCamera() {
        try {
            GmsDocumentScanning.getClient(options.build())
                .getStartScanIntent(requireActivity())
                .addOnSuccessListener { intentSender: IntentSender ->
                    scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                }
                .addOnFailureListener { e: Exception ->
                    Utils.showToast(requireContext(), e.localizedMessage ?: "Something went wrong ")
                    e.message?.let {
                        Log.e("error", it)
                    }
                }
        } catch (e: Exception) {
            Utils.showToast(requireContext(), e.localizedMessage ?: "Something went wrong ")
            e.stackTrace
        }

    }


    private fun documentHandleActivityResult(activityResult: ActivityResult) {
        try {
            val resultCode = activityResult.resultCode
            val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
            if (resultCode == Activity.RESULT_OK && result != null) {

                result.pdf?.uri?.path?.let { path ->
                    Utils.customAlert(
                        context = requireContext(),
                        title = "Document Save",
                        message = "Are you sure,you want to save this file"
                    ) {
                        try {
                            val originalFile = File(path)
                            val newFile = File(originalFile.parentFile, it)
                            val reNameFile = originalFile.renameTo(newFile)
                            if (reNameFile) {
                                val externalUri = FileProvider.getUriForFile(
                                    requireContext(),
                                    requireContext().packageName + ".provider",
                                    newFile
                                )

                                "reNameFile name generate pdf path $externalUri".logD()
                                val scanModel = ScanFile(
                                    fileName = newFile.name,
                                    fileUrl = externalUri.toString(),
                                    time = Utils.getCurrentTimeMills()
                                )
                                lifecycleScope.launch {
                                    scannerDB.scannerDao().insertScanFile(scanModel)
                                    Utils.shareFile(
                                        requireContext(),
                                        newFile.name,
                                        externalUri.toString()
                                    )
                                }
                            } else {
                                val externalUri = FileProvider.getUriForFile(
                                    requireContext(),
                                    requireContext().packageName + ".provider",
                                    File(path)
                                )
                                "pdf path $externalUri".logD()
                                val fileName = externalUri.toString().substringAfterLast("/")
                                val scanModel = ScanFile(
                                    fileName = fileName,
                                    fileUrl = externalUri.toString(),
                                    time = Utils.getCurrentTimeMills()
                                )
                                lifecycleScope.launch {
                                    scannerDB.scannerDao().insertScanFile(scanModel)
                                    Utils.shareFile(
                                        requireContext(),
                                        fileName,
                                        externalUri.toString()
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Utils.showToast(requireContext(), e.localizedMessage ?: "Something went wrong ")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Utils.showToast(requireContext(), e.localizedMessage ?: "Something went wrong ")
            e.stackTrace
        }
    }


    private fun scanAdapter() {
        val adapter = ScanAdapter(scanItems, requireContext()) { selectedItem ->
            when (selectedItem.title) {
                "Image to pdf" -> {
                    openCamera()
                }
                "Document to text" -> {
                    startActivity(Intent(requireContext(), LanguageSelectedActivity::class.java))
                }
                "ID card scan" -> {
                    openCamera()
                }
            }
        }
        binding.scanItemRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.scanItemRV.adapter = adapter
    }

    private fun pdfAdapter() {
        binding.pdfRV.layoutManager =
            LinearLayoutManager(requireContext())
        binding.pdfRV.setHasFixedSize(true)
        pdfAdapter = PdfAdapter(requireContext()) {
//            Utils.shareFile(requireContext(), it.fileName, it.fileUrl)
//            openPdfInPhone(it.fileUrl.toUri())

            MyBottomSheetFragment { selectedOption ->
                when (selectedOption) {
//                    "Rename" -> renamePdf(pdfFile)
//                    "Edit" -> editPdf(pdfFile)
//                    "Share" -> sharePdf(pdfFile)
//                    "Print" -> printPdf(pdfFile)
//                    "Delete" -> deletePdf(pdfFile)
                }
            }.show(requireActivity().supportFragmentManager, "PdfOptionsBottomSheet")
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

    private fun openPdfInPhone(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Open PDF with"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "No PDF viewer app found", Toast.LENGTH_SHORT).show()
        }
    }


}