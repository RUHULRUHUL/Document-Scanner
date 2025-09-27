package com.bugbd.pdfprinter.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bugbd.pdfprinter.R
import com.bugbd.pdfprinter.adapter.LanguageAdapter
import com.bugbd.pdfprinter.databinding.FragmentSettingsBinding
import com.bugbd.pdfprinter.ext.setCheckedRadio
import com.bugbd.pdfprinter.ext.setDarkLightThem
import com.bugbd.pdfprinter.ext.setLocale
import com.bugbd.pdfprinter.helper.Utils.Companion.getLanguageCode
import com.bugbd.pdfprinter.helper.drawerList
import com.bugbd.pdfprinter.local_bd.PreferenceManager
import com.bugbd.pdfprinter.adapter.OthersAdapter
import com.bugbd.pdfprinter.local_bd.ScannerDB
import com.bugbd.qrcode.model.LanguageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var scannerDB: ScannerDB
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSettingsBinding.inflate(layoutInflater)
        preferenceManager = PreferenceManager(requireContext())
        scannerDB = ScannerDB.getInstance(requireContext())

        if (isAdded){
            try {
                binding.appbar.tvTitle.text = requireContext().getString(R.string.settings)
                val context = requireContext()
                val packageManager = context.packageManager
                val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
                val appName = packageManager.getApplicationLabel(context.applicationInfo).toString()
                val versionName = packageInfo.versionName
                binding.appNameText.text = appName
                binding.appVersionText.text = "Version - $versionName"
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        clickEvent()

        return binding.root
    }

    private fun clickEvent() {
        binding.themSwitch.themSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                preferenceManager.set("them", "dark")
            } else {
                preferenceManager.set("them", "light")
            }
            setDarkLightThem(preferenceManager.get("them", "", String::class))
        }

        // ✅ Privacy Policy
        binding.privacyPolicy.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, "https://sites.google.com/view/qr-code-wifi-scanner".toUri())
            startActivity(intent)
        }

        // ✅ Share App
        binding.shareApp.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Check out ${getString(requireContext().applicationInfo.labelRes)}")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Download ${getString(requireContext().applicationInfo.labelRes)}:\nhttps://play.google.com/store/apps/details?id=${requireContext().packageName}"
                )
            }
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }

        // ✅ Rate & Review
        binding.rateApp.setOnClickListener {
            val uri = Uri.parse("market://details?id=${requireContext().packageName}")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")
                    )
                )
            }
        }

        binding.deleteLayout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear All Data")
                .setMessage("Do you want to clear all local data? This action cannot be undo.")
                .setPositiveButton("Yes") { _, _ ->
                    // Run DB clear on background thread
                    lifecycleScope.launch {
                        scannerDB.clearAllTables()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "All data cleared", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }


    }


    private fun getSelectedValue():String{
        return preferenceManager.get("lan","English",String::class)
    }
}