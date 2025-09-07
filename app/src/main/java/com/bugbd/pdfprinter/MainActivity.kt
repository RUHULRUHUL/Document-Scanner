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
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.google.android.material.card.MaterialCardView
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
        enableEdgeToEdge()
        showSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

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
                    GmsDocumentScannerOptions.RESULT_FORMAT_PDF
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
            openCamera()
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

        val options = listOf(
            binding.optionImageToText,
            binding.optionImageToPdf,
            binding.idCardLayout
        )

        fun highlightSelected(selected: View) {
            options.forEach { it.setBackgroundResource(R.drawable.unselected_bg) }
            selected.setBackgroundResource(R.drawable.selected_bg)
        }

        fun handleClick(view: View, action: () -> Unit) {
            highlightSelected(view)
            // ⏳ 200ms delay, so user sees the highlight
            view.postDelayed({
                dialog.dismiss()
                action()
            }, 500)
        }

        // Click Listeners
        binding.optionImageToText.setOnClickListener {
            handleClick(it) {
                startActivity(Intent(this, LanguageSelectedActivity::class.java))
            }
        }

        binding.optionImageToPdf.setOnClickListener {
            handleClick(it) { openCamera() }
        }

        binding.idCardLayout.setOnClickListener {
            handleClick(it) {
                Toast.makeText(this, "Id card scan clicked", Toast.LENGTH_SHORT).show()
                // এখানে তোমার action লিখো
            }
        }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
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