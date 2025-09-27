package com.bugbd.pdfprinter

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bugbd.pdfprinter.adapter.OnboardingAdapter
import com.bugbd.pdfprinter.databinding.ActivityOnboardingBinding
import com.bugbd.pdfprinter.ext.setDarkLightThem
import com.bugbd.pdfprinter.local_bd.PreferenceManager
import com.bugbd.pdfprinter.model.OnboardingItem
import kotlin.math.abs

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var adapter: OnboardingAdapter
    private lateinit var preferenceManager: PreferenceManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)
        setDarkLightThem(preferenceManager.get("them", "", String::class))
        val items = listOf(
            OnboardingItem(R.drawable.ic_document_scan, "Welcome", "This is your new scanner app."),
            OnboardingItem(R.drawable.ic_document_scan, "Scan Fast", "Quickly scan QR & Barcodes."),
            OnboardingItem(R.drawable.ic_document_scan, "Stay Organized", "Save and manage scans easily.")
        )

        adapter = OnboardingAdapter(items)
        binding.viewPager.adapter = adapter

        // Dots indicator setup
        binding.wormDotsIndicator.attachTo(binding.viewPager)

        // Page Transformer for Animation
        binding.viewPager.setPageTransformer { page, position ->
            page.alpha = 0.25f + (1 - abs(position))
            page.translationX = -50f * position
            page.scaleY = 0.85f + (1 - abs(position)) * 0.15f
        }

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem + 1 < adapter.itemCount) {
                binding.viewPager.currentItem += 1
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}
