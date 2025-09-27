package com.bugbd.pdfprinter.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
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
import com.bugbd.pdfprinter.ScanDetailsActivity
import com.bugbd.pdfprinter.adapter.ScanAdapter
import com.bugbd.pdfprinter.bottom_sheet.MyBottomSheetFragment
import com.bugbd.pdfprinter.ext.showToast
import com.bugbd.pdfprinter.helper.PdfPrinter
import com.bugbd.pdfprinter.helper.Utils.Companion.showRenameDialog
import com.bugbd.pdfprinter.helper.getBarCodeFormat
import com.bugbd.pdfprinter.helper.getBarcodeResult
import com.bugbd.pdfprinter.helper.openPdfInEditor
import com.bugbd.pdfprinter.helper.printPdf
import com.bugbd.pdfprinter.helper.renamePdfFile
import com.bugbd.pdfprinter.model.ScanFile
import com.bugbd.pdfprinter.model.ScanHistory
import com.bugbd.pdfprinter.model.scanItems
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
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
                    GmsDocumentScannerOptions.RESULT_FORMAT_PDF
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
        "scannerLauncher success".logD()
            val resultCode = activityResult.resultCode
            val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
            if (resultCode == Activity.RESULT_OK && result != null) {
                "resultCode ${Activity.RESULT_OK}".logD()
                result.pdf?.uri?.path?.let { path ->
                    "pdf generated path from mlkit $path".logD()
                    "pdf generate  file uri  ${result.pdf?.uri}".logD()
                    Utils.customAlert(
                        context = requireContext(),
                        title = "Document Save",
                        message = "Are you sure,you want to save this file"
                    ) {
                        try {
                            val originalFile = File(path)
                            "pdf generated name from mlkit ${originalFile.name}".logD()
                            val newFile = File(originalFile.parentFile, it)
                            val reNameFile = originalFile.renameTo(newFile)
                            if (reNameFile) {
                                val externalUri = FileProvider.getUriForFile(
                                    requireContext(),
                                    requireContext().packageName + ".provider",
                                    newFile
                                )

                                "rename this file  ${newFile.path}".logD()
                                "rename file uri  $externalUri".logD()
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
                                "rename not possible".logD()
                                val externalUri = FileProvider.getUriForFile(
                                    requireContext(),
                                    requireContext().packageName + ".provider",
                                    File(path)
                                )
                                "pdf uri $externalUri".logD()
                                val fileName = externalUri.toString().substringAfterLast("/")
                                "pdf file name $fileName".logD()
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
            when (selectedItem.id) {
                0 -> {
                    openCamera()
                }
                1 -> {
                    startActivity(Intent(requireContext(), LanguageSelectedActivity::class.java))
                }
                2 -> {
                    openCamera()
                }
                3 -> {
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
        }
        binding.scanItemRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.scanItemRV.adapter = adapter
    }

    private fun pdfAdapter() {
        binding.pdfRV.layoutManager =
            LinearLayoutManager(requireContext())
        binding.pdfRV.setHasFixedSize(true)
        pdfAdapter = PdfAdapter(requireContext()) {
            MyBottomSheetFragment { selectedOption ->
                when (selectedOption) {
                    "Rename" -> {
                        renamePdf(it)
                    }
                    "Edit" ->{
                        openPdfInEditor(requireContext(),it.fileUrl.toUri())
                    }
                    "View as pdf" -> {
                        browsePdfFile(it)
                    }
                    "Share" -> {
                        Utils.shareFile(
                            requireContext(),
                            it.fileName,
                            it.fileUrl
                        )
                    }
                    "Print" -> {
                        printPdf(requireContext(),it.fileUrl.toUri(),it.fileName)
                    }
                    "Delete" -> {
                        lifecycleScope.launch {
                            scannerDB.scannerDao().deleteFile(it)
                            Utils.showToast(requireContext(),"${it.fileName} successfully delete")
                        }
                    }
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

    private fun browsePdfFile(file: ScanFile) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(file.fileUrl.toUri(), "application/pdf")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            try {
                requireContext().startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "No PDF viewer installed", Toast.LENGTH_SHORT).show()
            }
    }

    fun renamePdf(file: ScanFile) {
        showRenameDialog(requireContext(),file.fileName){ fileName->
            try {
                val newFile = renamePdfFile(requireContext(), file.fileUrl.toUri(), fileName)
                if (newFile != null) {
                    val newFileUri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.provider",
                        newFile
                    )

                    val scanModel = ScanFile(
                        id = file.id,
                        fileName = newFile.name,
                        fileUrl = newFileUri.toString(),
                        time = Utils.getCurrentTimeMills()
                    )
                    Utils.showToast(requireContext(),"successfully rename file")
                    lifecycleScope.launch {
                        scannerDB.scannerDao().updateScanFile(scanModel)
                        Utils.shareFile(
                            requireContext(),
                            newFile.name,
                            newFileUri.toString()
                        )
                    }
                    "rename file name: ${newFile.name}".logD()
                    "rename file uri: $newFileUri".logD()
                    "newFilePath: ${newFile.path}".logD()
                    "new absolutePath: ${newFile.absolutePath}".logD()
                } else {
                    Log.e("Rename", "Rename failed")
                    Utils.showToast(requireContext(), "Something went wrong : Rename failed")
                }

            } catch (e: Exception) {
                Utils.showToast(requireContext(), e.localizedMessage ?: "Something went wrong ")
            }
        }
    }

}
