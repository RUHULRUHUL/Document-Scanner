package com.bugbd.pdfprinter.model

data class OnboardingItem(
    val imageRes: Int,
    val title: String,
    val description: String
)

data class PdfOption(val title: String, val iconRes: Int)

