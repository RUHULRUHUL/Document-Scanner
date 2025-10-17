package com.bugbd.pdfocr.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

data class OnboardingItem(
    val imageRes: Int,
    val title: String,
    val description: String
)

data class PdfOption(val title: String, val iconRes: Int)

@Entity
data class ScanHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val time:Long
): Serializable

