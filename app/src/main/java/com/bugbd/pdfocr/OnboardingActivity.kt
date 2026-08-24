package com.bugbd.pdfocr

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.ActivityOptions
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.bugbd.pdfocr.adapter.OnboardingAdapter
import com.bugbd.pdfocr.databinding.ActivityOnboardingBinding
import com.bugbd.pdfocr.ext.setDarkLightThem
import com.bugbd.pdfocr.helper.Constants
import com.bugbd.pdfocr.local_bd.PreferenceManager
import com.bugbd.pdfocr.model.OnboardingItem
import kotlin.math.abs

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var adapter: OnboardingAdapter
    private lateinit var preferenceManager: PreferenceManager
    private var pulsingAnimator: ObjectAnimator? = null

    override fun onDestroy() {
        pulsingAnimator?.cancel()
        super.onDestroy()
    }

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
                "Convert multiple images into PDF file with high quality output easily.",
                gifRes = R.drawable.image_to_pdf
            ),
            OnboardingItem(
                R.drawable.ic_ocr_scanner,
                "Text Recognizer OCR",
                "Extract text from any language using powerful OCR technology.",
                gifRes = R.drawable.ocr_animation
            ),
            OnboardingItem(
                R.drawable.id_card_scanner,
                "Smart Card Scanner",
                "Quickly scan and read smart cards for faster access and better organization.",
                gifRes = R.drawable.card_scan
            )
        )

        adapter = OnboardingAdapter(items)
        binding.viewPager.adapter = adapter

        // Dots indicator setup
        binding.wormDotsIndicator.attachTo(binding.viewPager)

        // Page Transformer for Animation
        binding.viewPager.setPageTransformer { page, position ->
            page.apply {
                val absPos = abs(position)
                // Scale the page down (between 0.85 and 1.0)
                val scale = 0.85f + (1 - absPos) * 0.15f
                scaleX = scale
                scaleY = scale
                
                // Fade the page relative to its distance from the center
                alpha = 0.5f + (1 - absPos) * 0.5f
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (position == items.size - 1) {
                    binding.btnNext.text = "Get Started"
                    binding.btnNext.backgroundTintList = ColorStateList.valueOf(getColor(R.color.green))
                    binding.btnNext.setTextColor(getColor(R.color.white))
                    binding.btnNext.elevation = 8f
                    
                    // Start pulsing animation for "Get Started" to highlight it
                    pulsingAnimator?.cancel()
                    pulsingAnimator = ObjectAnimator.ofPropertyValuesHolder(
                        binding.btnNext,
                        PropertyValuesHolder.ofFloat("scaleX", 1.08f),
                        PropertyValuesHolder.ofFloat("scaleY", 1.08f)
                    ).apply {
                        duration = 600
                        repeatCount = ObjectAnimator.INFINITE
                        repeatMode = ObjectAnimator.REVERSE
                        start()
                    }
                } else {
                    binding.btnNext.text = "Next"
                    binding.btnNext.backgroundTintList = ColorStateList.valueOf(getColor(R.color.onBackground))
                    binding.btnNext.setTextColor(getColor(R.color.black))
                    binding.btnNext.elevation = 2f
                    
                    // Stop pulsing and reset scale
                    pulsingAnimator?.cancel()
                    binding.btnNext.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
                }
            }
        })

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem + 1 < items.size) {
                binding.viewPager.currentItem += 1
            } else {
                preferenceManager.set(Constants.firstTimeVisit, true)
                val intent = Intent(this, MainActivity::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val options = ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
                    startActivity(intent, options.toBundle())
                } else {
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
                finish()
            }
        }
    }
}
