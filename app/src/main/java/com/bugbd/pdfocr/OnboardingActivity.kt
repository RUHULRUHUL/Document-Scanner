package com.bugbd.pdfocr

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bugbd.pdfocr.adapter.OnboardingAdapter
import com.bugbd.pdfocr.databinding.ActivityOnboardingBinding
import com.bugbd.pdfocr.ext.setDarkLightThem
import com.bugbd.pdfocr.helper.Constants
import com.bugbd.pdfocr.local_bd.PreferenceManager
import com.bugbd.pdfocr.model.OnboardingItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var adapter: OnboardingAdapter
    private lateinit var preferenceManager: PreferenceManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        preferenceManager = PreferenceManager(this)
        if (preferenceManager.get(Constants.firstTimeVisit,false, Boolean::class)){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        setDarkLightThem(preferenceManager.get("them", "", String::class))
        val items = listOf(
            OnboardingItem(
                R.drawable.ic_pdf_scanner,
                "Multiple Image to PDF",
                "Convert multiple images into PDF file"
            ),
            OnboardingItem(
                R.drawable.ic_ocr_scanner,
                "Text Recognizer OCR",
                "Extract text from any language using powerful OCR technology."
            ),
            OnboardingItem(
                R.drawable.id_card_scanner,
                "Smart Card Scanner",
                "Quickly scan and read smart cards for faster access."
            )
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

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (position == 2) {
                    // last onboarding page
                    binding.btnNext.setBackgroundColor(
                        ContextCompat.getColor(this@OnboardingActivity, R.color.green)
                    )
                    binding.btnNext.text = "Get Started"
                    lifecycleScope.launch {
                        delay(500)
                        preferenceManager.set(Constants.firstTimeVisit,true)
                        startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                        finish()
                    }

                } else {
                    // other pages
                    binding.btnNext.setBackgroundColor(
                        ContextCompat.getColor(this@OnboardingActivity, R.color.gray)
                    )
                    binding.btnNext.text = "Next"
                }
            }
        })

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem + 1 < adapter.itemCount) {
                binding.viewPager.currentItem += 1
            } else {
                preferenceManager.set(Constants.firstTimeVisit,true)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}
