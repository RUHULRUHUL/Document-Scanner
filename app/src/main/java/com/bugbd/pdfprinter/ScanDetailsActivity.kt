package com.bugbd.pdfprinter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bugbd.pdfprinter.bottom_sheet.FileSaveOptionSelectBottom
import com.bugbd.pdfprinter.bottom_sheet.MyBottomSheetFragment
import com.bugbd.pdfprinter.databinding.ActivityScanDetailsBinding
import com.bugbd.pdfprinter.helper.Utils
import com.bugbd.pdfprinter.helper.saveTextAsPdf
import com.bugbd.pdfprinter.helper.saveTextAsTxt
import com.bugbd.pdfprinter.local_bd.ScannerDB
import com.bugbd.pdfprinter.model.ScanFile
import kotlinx.coroutines.launch

class ScanDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanDetailsBinding
    private lateinit var scannerDB: ScannerDB


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEditableText()
        loadScannedText()
        scannerDB = ScannerDB.getInstance(this)
        binding.saveIcon.setOnClickListener {
            FileSaveOptionSelectBottom { selectedOption ->
                when (selectedOption) {
                    "Save as pdf" -> {
                        val fileName = "document-save-${System.currentTimeMillis()}.pdf"
                        saveTextAsPdf(context = this,fileName,binding.editTextContent.text.toString()){ downloadUri ->
                            val scanModel = ScanFile(
                                fileName = fileName,
                                fileUrl = downloadUri,
                                time = Utils.getCurrentTimeMills()
                            )
                            lifecycleScope.launch {
                                scannerDB.scannerDao().insertScanFile(scanModel)
                            }
                        }
                    }
                    "Save as txt" -> {
                        val fileName = "Txt-${System.currentTimeMillis()}.txt"
                        val uri = saveTextAsTxt(context = this,fileName,binding.editTextContent.text.toString())
                        val scanModel = ScanFile(
                            fileName = fileName,
                            fileUrl = uri.toString(),
                            time = Utils.getCurrentTimeMills()
                        )
                        lifecycleScope.launch {
                            scannerDB.scannerDao().insertScanFile(scanModel)
                        }
                    }
                    "Save as docx" -> {

                    }
                }
            }.show(supportFragmentManager, "PdfOptionsBottomSheet")
        }
    }

    // Editable full-screen text
    private fun setupEditableText() {
        val scannedText = intent.getStringExtra("scanned_text") ?: ""
        binding.editTextContent.setText(scannedText)
        binding.editTextContent.isVerticalScrollBarEnabled = true
        binding.editTextContent.movementMethod = ScrollingMovementMethod()
    }

    // Load scanned text (from intent or db)
    private fun loadScannedText() {
        val scannedText = intent.getStringExtra("scanned_text") ?: ""
        binding.editTextContent.setText(scannedText)
    }

    // --- Actions ---
    private fun exportToPdf() {
//        val text = binding.editTextContent.text.toString()
//        if (text.isEmpty()) {
//            return
//        }
//
//        try {
//            // Create a new document
//            val pdfDocument = PdfDocument()
//
//            // Page info (A4 size: 595x842 points at 72 dpi)
//            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
//            val page = pdfDocument.startPage(pageInfo)
//
//            val canvas = page.canvas
//
//            // Paint for text
//            val paint = Paint().apply {
//                color = Color.BLACK
//                textSize = 14f
//                isAntiAlias = true
//            }
//
//            val x = 40f
//            var y = 50f
//
//            // Break text line by line
//            val textLines = text.split("\n")
//            for (line in textLines) {
//                val wrapped = TextUtils.split(line, 80) // প্রতি লাইনে 80 অক্ষর
//                for (segment in wrapped) {
//                    if (y > 800) { // page overflow হলে নতুন পেজ
//                        pdfDocument.finishPage(page)
//                        val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.count() + 1).create()
//                        val newPage = pdfDocument.startPage(newPageInfo)
//                        canvas.setBitmap(newPage.)
//                        y = 50f
//                    }
//                    canvas.drawText(segment, x, y, paint)
//                    y += paint.textSize + 6f
//                }
//            }
//
//            pdfDocument.finishPage(page)
//
//            // Save file
//            val fileName = "ScannedText_${System.currentTimeMillis()}.pdf"
//            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
//
//            pdfDocument.writeTo(FileOutputStream(file))
//            pdfDocument.close()
//
//            showToast("PDF saved: ${file.absolutePath}")
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//            showToast("Failed: ${e.message}")
//        }
    }

    private fun exportToWord() {
        val text = binding.editTextContent.text.toString()
//        Toast.makeText(this, "Exporting to Word...", Toast.LENGTH_SHORT).show()
        // TODO: implement Word export
    }

    private fun copyTextToClipboard() {
        val text = binding.editTextContent.text.toString()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Scanned Text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copied to Clipboard", Toast.LENGTH_SHORT).show()
    }
}
