package com.bugbd.pdfocr.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bugbd.pdfocr.LanguageSelectedActivity
import com.bugbd.pdfocr.adapter.PdfAdapter
import com.bugbd.pdfocr.adapter.ScanAdapter
import com.bugbd.pdfocr.bottom_sheet.BottomSheetForImage
import com.bugbd.pdfocr.bottom_sheet.MyBottomSheetFragment
import com.bugbd.pdfocr.local_bd.ScannerDB as LocalScannerDB
import com.bugbd.pdfocr.local_bd.PreferenceManager
import com.bugbd.pdfocr.databinding.FragmentHomeBinding
import com.bugbd.pdfocr.helper.Constants
import com.bugbd.pdfocr.helper.*
import com.bugbd.pdfocr.helper.Utils.Companion.saveImageToGallery
import com.bugbd.pdfocr.helper.Utils.Companion.shareImage
import com.bugbd.pdfocr.helper.Utils.Companion.showRenameDialog
import com.bugbd.pdfocr.model.ScanFile
import com.bugbd.pdfocr.model.ScanHistory
import com.bugbd.pdfocr.model.scanItems
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import io.getstream.photoview.dialog.PhotoViewDialog
import kotlinx.coroutines.launch
import java.io.File

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    lateinit var preferenceManager: PreferenceManager
    lateinit var pdfAdapter: PdfAdapter
    lateinit var scannerDB: LocalScannerDB

    lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    lateinit var imageCaptureLauncher: ActivityResultLauncher<IntentSenderRequest>

    lateinit var options: GmsDocumentScannerOptions.Builder
    lateinit var imageCaptureOptions: GmsDocumentScannerOptions.Builder
    lateinit var optionCardScan: GmsDocumentScannerOptions.Builder

    private val barCodeOptions = GmsBarcodeScannerOptions.Builder()
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
        scannerDB = LocalScannerDB.getInstance(requireContext())
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
        binding.fabStartScan.setOnClickListener {
            openCamera()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when (hour) {
            in 0..11 -> "Good Morning!"
            in 12..15 -> "Good Afternoon!"
            in 16..20 -> "Good Evening!"
            else -> "Good Night!"
        }

        scannerLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                documentHandleActivityResult(result)
            }

        imageCaptureLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                imageCaptureActivityResult(result)
            }

        try {
            options = GmsDocumentScannerOptions.Builder()
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                .setGalleryImportAllowed(true)

            imageCaptureOptions = GmsDocumentScannerOptions.Builder()
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                .setGalleryImportAllowed(true)
                .setPageLimit(10)

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
        val scanner = GmsDocumentScanning.getClient(options.build())
        scanner.getStartScanIntent(requireActivity())
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    private fun openCameraForCaptureImage() {
        val scanner = GmsDocumentScanning.getClient(imageCaptureOptions.build())
        scanner.getStartScanIntent(requireActivity())
            .addOnSuccessListener { intentSender ->
                imageCaptureLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    private fun openCameraForIdCardScan() {
        val scanner = GmsDocumentScanning.getClient(optionCardScan.build())
        scanner.getStartScanIntent(requireActivity())
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    private fun documentHandleActivityResult(result: ActivityResult) {
        try {
            val resultCode = result.resultCode
            val data = result.data
            if (resultCode == android.app.Activity.RESULT_OK) {
                val resultData = com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult.fromActivityResultIntent(data)
                resultData?.pdf?.let { pdf ->
                    val pdfUri = pdf.uri
                    val scanModel = ScanFile(
                        id = 0,
                        fileName = "PDF_${System.currentTimeMillis()}.pdf",
                        fileUrl = pdfUri.toString(),
                        time = Utils.getCurrentTimeMills()
                    )

                    lifecycleScope.launch {
                        scannerDB.scannerDao().insertScanFile(scanModel)
                        val file = Utils.getSafeFile(pdfUri.toString())
                        Utils.savePdfToGallery(requireContext(), file)
                    }
                }
            }
        } catch (e: Exception) {
            Utils.showToast(requireContext(), e.localizedMessage ?: "Something went wrong ")
            e.printStackTrace()
        }
    }

    private fun imageCaptureActivityResult(result: ActivityResult) {
        try {
            val resultCode = result.resultCode
            val data = result.data
            if (resultCode == android.app.Activity.RESULT_OK) {
                val resultData = com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult.fromActivityResultIntent(data)
                resultData?.pages?.let { pages ->
                    for (page in pages) {
                        val imageUri = page.imageUri
                        val scanModel = ScanFile(
                            id = 0,
                            fileName = "IMG_${System.currentTimeMillis()}.jpeg",
                            fileUrl = imageUri.toString(),
                            time = Utils.getCurrentTimeMills()
                        )

                        lifecycleScope.launch {
                            scannerDB.scannerDao().insertScanFile(scanModel)
                            val file = Utils.getSafeFile(imageUri.toString())
                            Utils.saveImageToGallery(requireContext(), file)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Utils.showToast(requireContext(), e.localizedMessage ?: "Something went wrong ")
            e.printStackTrace()
        }
    }


    private fun scanAdapter() {
        val adapter = ScanAdapter(scanItems, requireContext()) { selectedItem ->
            when (selectedItem.id) {
                0 -> {
                    openCamera()
                }
                1 -> {
                    openCameraForCaptureImage()
                }
                2 -> {
                     startActivity(Intent(requireContext(), LanguageSelectedActivity::class.java))
                }
                3 -> {
                    openCameraForIdCardScan()
                }
                4 -> {
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
        binding.pdfRV.layoutManager = LinearLayoutManager(requireContext())
        binding.pdfRV.setHasFixedSize(true)
        pdfAdapter = PdfAdapter(requireContext()) {
            if (it.fileName.contains(".jpeg", ignoreCase = true) ||
                it.fileName.contains(".jpg", ignoreCase = true) ||
                it.fileName.contains(".png", ignoreCase = true)
            ) {
                BottomSheetForImage { selectedOption ->
                    when (selectedOption) {
                        "Share" -> {
                            "image uri: ${it.fileUrl}".logD()
                            shareImage(requireContext(), it.fileUrl.toString())
                        }

                        "Save to gallery" -> {
                            "image uri: ${it.fileUrl}".logD()
                            val file = Utils.getSafeFile(it.fileUrl)
                            saveImageToGallery(requireContext(), file)
                        }

                        "Delete" -> {
                            lifecycleScope.launch {
                                scannerDB.scannerDao().deleteFile(it)
                                Utils.showToast(
                                    requireContext(),
                                    "${it.fileName} successfully delete"
                                )
                            }
                        }

                        "Show" -> {
                            val imageUrls = listOf(it.fileUrl)
                            PhotoViewDialog.Builder(
                                context = requireContext(),
                                images = imageUrls
                            ) { imageView, url ->
                                Glide.with(this)
                                    .load(url)
                                    .into(imageView)
                            }.build().show()
                        }
                    }
                }.show(requireActivity().supportFragmentManager, "BottomSheetForImage")
            } else {
                MyBottomSheetFragment { selectedOption ->
                    when (selectedOption) {
                        "Rename" -> {
                            renamePdf(it)
                        }

                        "Edit" -> {
                            openPdfInEditor(requireContext(), it.fileUrl.toUri())
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
                            printPdf(requireContext(), it.fileUrl.toUri(), it.fileName)
                        }

                        "Delete" -> {
                            lifecycleScope.launch {
                                scannerDB.scannerDao().deleteFile(it)
                                Utils.showToast(
                                    requireContext(),
                                    "${it.fileName} successfully delete"
                                )
                            }
                        }
                    }
                }.show(requireActivity().supportFragmentManager, "PdfOptionsBottomSheet")
            }
        }
        binding.pdfRV.adapter = pdfAdapter

        // Observe database changes and update the adapter automatically
        scannerDB.scannerDao().getScanFileList().observe(viewLifecycleOwner) { list ->
            pdfAdapter.updateItems(list)
            
            // Toggle empty state visibility based on data
            if (list.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.tvRecentHeader.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.tvRecentHeader.visibility = View.VISIBLE
            }
        }
    }

    private fun browsePdfFile(file: ScanFile) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = if (file.fileUrl.startsWith("content://")) {
            file.fileUrl.toUri()
        } else {
            val fileToOpen = Utils.getSafeFile(file.fileUrl)
            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                fileToOpen
            )
        }
        intent.setDataAndType(uri, "application/pdf")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY

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
                    }
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