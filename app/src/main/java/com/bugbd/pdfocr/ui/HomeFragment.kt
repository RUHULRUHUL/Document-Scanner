package com.bugbd.pdfocr.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bugbd.pdfocr.adapter.PdfAdapter
import com.bugbd.pdfocr.databinding.FragmentHomeBinding
import com.bugbd.pdfocr.helper.Utils
import com.bugbd.pdfocr.helper.logD
import com.bugbd.pdfocr.local_bd.PreferenceManager
import com.bugbd.pdfocr.local_bd.ScannerDB
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.bugbd.pdfocr.LanguageSelectedActivity
import com.bugbd.pdfocr.ScanDetailsActivity
import com.bugbd.pdfocr.adapter.ScanAdapter
import com.bugbd.pdfocr.bottom_sheet.MyBottomSheetFragment
import com.bugbd.pdfocr.helper.Constants
import com.bugbd.pdfocr.helper.Utils.Companion.showRenameDialog
import com.bugbd.pdfocr.helper.getBarCodeFormat
import com.bugbd.pdfocr.helper.getBarcodeResult
import com.bugbd.pdfocr.helper.openPdfInEditor
import com.bugbd.pdfocr.helper.printPdf
import com.bugbd.pdfocr.helper.renamePdfFile
import com.bugbd.pdfocr.model.ScanFile
import com.bugbd.pdfocr.model.ScanHistory
import com.bugbd.pdfocr.model.scanItems
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import kotlin.coroutines.Continuation

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var pdfAdapter: PdfAdapter
    private lateinit var scannerDB: ScannerDB

    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var options: GmsDocumentScannerOptions.Builder
    private lateinit var optionCardScan: GmsDocumentScannerOptions.Builder

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
        if (preferenceManager.get(Constants.isFirstTimeAutoCameraOpen, true, Boolean::class)){
            preferenceManager.set(Constants.isFirstTimeAutoCameraOpen, false)
            openCamera()
        }
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
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                .setGalleryImportAllowed(true)

            //id card scanner
            optionCardScan = GmsDocumentScannerOptions.Builder()
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE_WITH_FILTER)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                .setGalleryImportAllowed(true)
                .setPageLimit(2)
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

    private fun openCameraForIdCardScan() {
        try {
            GmsDocumentScanning.getClient(optionCardScan.build())
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
                    openCameraForIdCardScan()
                }
                3 -> {
                    startQRCodeScan()
                }
            }
        }
        binding.scanItemRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.scanItemRV.adapter = adapter
    }

    private fun startQRCodeScan() {
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
                Utils.showToast(requireContext(),"scan cancel" )
            }
            .addOnFailureListener { e ->
                Utils.showToast(requireContext(),e.message?:"Something went wrong" )
            }
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
                    pdfAdapter.updateItems(it)
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
