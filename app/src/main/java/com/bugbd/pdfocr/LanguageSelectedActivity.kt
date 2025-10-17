package com.bugbd.pdfocr

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bugbd.pdfocr.adapter.LanguageAdapter
import com.bugbd.pdfocr.databinding.ActivityLanguageSelectedBinding
import com.bugbd.pdfocr.helper.PermissionHelper
import com.bugbd.pdfocr.helper.Utils
import com.bugbd.pdfocr.helper.Utils.Companion.dismissDialog
import com.bugbd.pdfocr.helper.Utils.Companion.showDialog
import com.bugbd.pdfocr.helper.getRequiredPermissions
import com.bugbd.pdfocr.helper.intiProgressDialog
import com.bugbd.pdfocr.model.LanguageSupported
import com.bugbd.pdfocr.model.supportedLanguagesV2
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class LanguageSelectedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageSelectedBinding

    private lateinit var progressDialog: Dialog

    private lateinit var permissionHelper: PermissionHelper
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var options: GmsDocumentScannerOptions.Builder

    var recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)


    private var selectLanguageObj: LanguageSupported? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflate binding
        binding = ActivityLanguageSelectedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initialize()
        clickEvent()
        setupRecyclerView()

    }

    private fun clickEvent() {
        binding.icBackImg.setOnClickListener {
            finish()
        }
        binding.gotoScan.setOnClickListener {
            selectLanguageObj?.let { item ->
                recognizer = if (item.script.equals("Latn",false)){
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                }else if (item.script.equals("Hans",false)){
                    TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
                }else if (item.script.equals("Deva",false)){
                    TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
                }else if (item.script.equals("Jpan",false)){
                    TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
                }else if (item.script.equals("Kore",false)){
                    TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
                }else{
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                }
                openCamera()
            }

        }
    }

    private fun initialize() {
        progressDialog = intiProgressDialog(context = this, layoutInflater = layoutInflater)
        val htmlText = """
            <b>Auto-detect:</b> Latin script languages (e.g., English, Spanish, French)<br><br>
            <b>Manual selection required:</b><br>
            - Devanagari languages (Bangla, Hindi, Marathi, Nepali)<br>
            - CJK languages (Chinese, Japanese, Korean)<br><br>
            Select manually to ensure accurate text recognition.
        """.trimIndent()

        binding.pdfLabelTxt.text =
            Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)

        permissionHelper = PermissionHelper(context = this, activity = this) {}
        permissionHelper.initLauncher {
            permissionLauncher = it
            permissionHelper.checkAndRequestPermissions(getRequiredPermissions(), permissionLauncher)
        }

        scannerLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                imageHandleActivityResult(result)
            }
        try {
            options = GmsDocumentScannerOptions.Builder()
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
                .setResultFormats(
                    GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
                )
                .setGalleryImportAllowed(true)
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                .setPageLimit(1)
        } catch (e: Exception) {
            Utils.showToast(this, e.localizedMessage ?: "Something went wrong ")
            e.printStackTrace()
        }
    }

    private fun setupRecyclerView() {

        val adapter = LanguageAdapter(supportedLanguagesV2, this) { selectedItem ->
            selectLanguageObj = selectedItem
            binding.gotoScan.text = "Open camera:  ${selectedItem.name}"
        }

        binding.languageRV.layoutManager = GridLayoutManager(this, 2)
        binding.languageRV.adapter = adapter
    }

    private fun openCamera() {
        try {
            GmsDocumentScanning.getClient(options.build())
                .getStartScanIntent(this)
                .addOnSuccessListener { intentSender: IntentSender ->
                    scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                }
                .addOnFailureListener { e: Exception ->
                    Utils.showToast(this, e.localizedMessage ?: "Something went wrong ")
                    e.message?.let {
                        Log.e("error", it)
                    }
                }
        } catch (e: Exception) {
            Utils.showToast(this, e.localizedMessage ?: "Something went wrong ")
            e.stackTrace
        }

    }

    private fun imageHandleActivityResult(activityResult: ActivityResult) {
        progressDialog.showDialog()
        val resultCode = activityResult.resultCode
        val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
        if (resultCode == Activity.RESULT_OK && result != null) {
            result.pages.let { pages->
                val imageUri = pages?.get(0)?.imageUri
                if (imageUri.toString().isNotEmpty()){
                    imageUri?.let {
                        startTextRecognition(it)
                    }
                }else{
                    progressDialog.dismissDialog()
                }
            }
        }else{
            progressDialog.dismissDialog()
        }
    }

    private fun startTextRecognition(uri: Uri) {
        val image = InputImage.fromFilePath(this, uri)
        val recognizer = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                progressDialog.dismissDialog()
                val allText = visionText.text
                val intent = Intent(this, ScanDetailsActivity::class.java)
                intent.putExtra("scanned_text", allText)  // ✅ টেক্সট পাঠানো হচ্ছে
                startActivity(intent)
                Log.d("OCR", "Extracted: $allText")
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismissDialog()
                Utils.showToast(this, e.localizedMessage ?: "Something went wrong ")
                Log.e("OCR", "Error: ${e.message}")
            }

    }
}
