package com.bugbd.pdfprinter

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.bugbd.pdfprinter.databinding.ActivityMainBinding
import com.bugbd.pdfprinter.databinding.PopupDialogBinding
import com.bugbd.pdfprinter.ext.gone
import com.bugbd.pdfprinter.ext.setDarkLightThem
import com.bugbd.pdfprinter.ext.visible
import com.bugbd.pdfprinter.local_bd.PreferenceManager
import com.bugbd.pdfprinter.viewmodel.HomeViewModel
import com.bugbd.pdfprinter.helper.PermissionHelper
import com.bugbd.pdfprinter.helper.Utils
import com.bugbd.pdfprinter.helper.Utils.Companion.intiProgressDialog
import com.bugbd.pdfprinter.helper.getPdfs
import com.bugbd.pdfprinter.helper.getRequiredPermissions
import com.bugbd.pdfprinter.helper.logD
import com.bugbd.pdfprinter.local_bd.ScannerDB
import com.bugbd.qrcode.model.ScanFile
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var permissionHelper: PermissionHelper
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var options: GmsDocumentScannerOptions.Builder

    private lateinit var scannerDB: ScannerDB
    private lateinit var progressDialog: Dialog



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initInstance()
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.homeFragmentContainer) as NavHostFragment
        navController = navHostFragment.navController
        initView()
    }

    private fun initInstance() {
        progressDialog = intiProgressDialog(context = this, layoutInflater = layoutInflater)
        scannerDB = ScannerDB.getInstance(this)
        preferenceManager = PreferenceManager(this)
        permissionHelper = PermissionHelper(context = this, activity = this) {}
        permissionHelper.initLauncher {
            permissionLauncher = it
            checkPermissionUser()
        }

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
            Utils.showToast(this, e.localizedMessage ?: "Something went wrong ")
            e.printStackTrace()
        }
    }

    private fun checkPermissionUser() {
        permissionHelper.checkAndRequestPermissions(getRequiredPermissions(), permissionLauncher)
    }


    private fun showSplashScreen() {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.isLoading.value
            }
        }
        supportActionBar?.hide()
    }

    private fun initView() {
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->

            when (destination.id) {
                R.id.homeFragment -> {
                    binding.bottomNavigation.visible()
                    binding.fabRecharge.visible()
                }
                R.id.QRScanActivity -> {
//                    Utils.showToast(this,"open camera")
//                    openCamera()
//                    binding.bottomNavigation.gone()
//                    binding.fabRecharge.gone()
                }

                R.id.settingsFragment -> {
                    binding.bottomNavigation.visible()
                    binding.fabRecharge.visible()
                }

                else -> {
                    binding.bottomNavigation.gone()
                    binding.fabRecharge.gone()
                }


            }
        }

        binding.fabRecharge.setOnClickListener {
            showScanOptionsPopup()
        }
        setDarkLightThem(preferenceManager.get("them", "", String::class))
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

    private fun showScanOptionsPopup() {
        val binding = PopupDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(binding.root)
            .create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        // Click Listeners
        binding.optionImageToText.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, LanguageSelectedActivity::class.java))
        }

        binding.optionImageToPdf.setOnClickListener {
            dialog.dismiss()
            openCamera()
        }

        dialog.show()
    }



    private fun handleActivityResult(activityResult: ActivityResult) {
        val resultCode = activityResult.resultCode
        val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
        if (resultCode == Activity.RESULT_OK && result != null) {
            result.pages.let { pages->
                val imageUri = pages?.get(0)?.imageUri
                imageUri?.let {
                    startTextRecognition(it)
                }
            }
        }
    }

    private fun startTextRecognition(uri: Uri) {
            val image = InputImage.fromFilePath(this, uri)
//            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val recognizer = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
        recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val allText = visionText.text
                    Log.d("OCR", "Extracted: $allText")
                }
                .addOnFailureListener { e ->
                    Log.e("OCR", "Error: ${e.message}")
                }

    }


    private fun documentHandleActivityResult(activityResult: ActivityResult) {
        try {
            val resultCode = activityResult.resultCode
            val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
            if (resultCode == Activity.RESULT_OK && result != null) {

                result.pdf?.uri?.path?.let { path ->
                    Utils.customAlert(
                        context = this,
                        title = "Document Save",
                        message = "Are you sure,you want to save this file"
                    ) {
                        try {
                            val originalFile = File(path)
                            val newFile = File(originalFile.parentFile, it)
                            val reNameFile = originalFile.renameTo(newFile)
                            if (reNameFile) {
                                val externalUri = FileProvider.getUriForFile(
                                    this,
                                    this.packageName + ".provider",
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
                                        this@MainActivity,
                                        newFile.name,
                                        externalUri.toString()
                                    )
                                }
                            } else {
                                val externalUri = FileProvider.getUriForFile(
                                    this,
                                    this.packageName + ".provider",
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
                                        this@MainActivity,
                                        fileName,
                                        externalUri.toString()
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Utils.showToast(this, e.localizedMessage ?: "Something went wrong ")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Utils.showToast(this, e.localizedMessage ?: "Something went wrong ")
            e.stackTrace
        }
    }

}