package com.bugbd.pdfprinter

import android.app.Dialog
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
import com.bugbd.pdfprinter.helper.Utils.Companion.dismissDialog
import com.bugbd.pdfprinter.helper.Utils.Companion.showDialog
import com.bugbd.pdfprinter.helper.intiProgressDialog
import com.bugbd.pdfprinter.helper.saveTextAsPdf
import com.bugbd.pdfprinter.helper.saveTextAsTxt
import com.bugbd.pdfprinter.local_bd.ScannerDB
import com.bugbd.pdfprinter.model.ScanFile
import kotlinx.coroutines.launch

class ScanDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanDetailsBinding
    private lateinit var scannerDB: ScannerDB
    private lateinit var progressDialog: Dialog



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = intiProgressDialog(context = this, layoutInflater = layoutInflater)
        setupEditableText()
        loadScannedText()
        scannerDB = ScannerDB.getInstance(this)
        binding.saveIcon.setOnClickListener {
            FileSaveOptionSelectBottom { selectedOption ->
                when (selectedOption) {
                    "Save as pdf" -> {
                        val fileName = "document-save-${System.currentTimeMillis()}.pdf"
                        saveTextAsPdf(context = this,fileName,binding.editTextContent.text.toString(), onSave = { downloadUri ->
                            val scanModel = ScanFile(
                                fileName = fileName,
                                fileUrl = downloadUri,
                                time = Utils.getCurrentTimeMills()
                            )
                            lifecycleScope.launch {
                                scannerDB.scannerDao().insertScanFile(scanModel)
                            }
                        }, isLoading = { isLoading ->
                            if (isLoading){
                                progressDialog.showDialog()
                            }else{
                                progressDialog.dismissDialog()
                                finish()
                            }
                        })
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
        binding.icBackImg.setOnClickListener {
            finish()
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
