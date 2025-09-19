package com.bugbd.pdfprinter.model

import android.icu.text.CaseMap.Title
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bugbd.pdfprinter.R
import java.io.Serializable


@Entity
data class ScanModel(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val type: String="",
    var scanValue: String="",
    var time:Long = 0,
    var image:String = ""
): Serializable

@Entity
data class ScanFile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fileName: String="",
    val fileUrl: String="",
    val filePath: String = "",
    val time:Long = 0
)

data class WidgetData(
    var title: String = "",
    var description: String = "",
    var time:Long = 0
)


data class CommonModel(
    var text: String = "",
)
data class WifiModel(
    var ssid: String = "",
    var type: String = "",
    var password: String = "",
)

data class SmsModel(
    var phone: String = "",
    var text: String = "",
)
data class BarCodeModel(
    var text: String = "",
    var format: String = "",
)

data class LanguageSupported(
    var name: String = "",
    var languageCode: String = "",
    var script: String = "",
    var isSelected: Boolean = true
)

//val supportedLanguages = listOf(
//    LanguageSupported("Auto Detect", "af", "Latn"),
//    LanguageSupported("Afrikaans", "af", "Latn"),
//    LanguageSupported("Albanian", "sq", "Latn"),
//    LanguageSupported("Catalan", "ca", "Latn"),
//    LanguageSupported("Chinese", "zh", "Hans/Hant; supported in v2"),
//    LanguageSupported("Croatian", "hr", "Latn"),
//    LanguageSupported("Czech", "cs", "Latn"),
//    LanguageSupported("Danish", "da", "Latn"),
//    LanguageSupported("Dutch", "nl", "Latn"),
//    LanguageSupported("English", "en", "Latn; American"),
//    LanguageSupported("Estonian", "et", "Latn"),
//    LanguageSupported("Filipino", "fil", "Latn"),
//    LanguageSupported("Finnish", "fi", "Latn"),
//    LanguageSupported("French", "fr", "Latn; European"),
//    LanguageSupported("German", "de", "Latn"),
//    LanguageSupported("Hindi", "hi", "Deva; supported in v2"),
//    LanguageSupported("Hungarian", "hu", "Latn"),
//    LanguageSupported("Icelandic", "is", "Latn"),
//    LanguageSupported("Indonesian", "id", "Latn"),
//    LanguageSupported("Italian", "it", "Latn"),
//    LanguageSupported("Japanese", "ja", "Jpan; supported in v2"),
//    LanguageSupported("Korean", "ko", "Kore; supported in v2"),
//    LanguageSupported("Latvian", "lv", "Latn"),
//    LanguageSupported("Lithuanian", "lt", "Latn"),
//    LanguageSupported("Malay", "ms", "Latn"),
//    LanguageSupported("Marathi", "mr", "Deva; supported in v2"),
//    LanguageSupported("Nepali", "ne", "Deva; supported in v2"),
//    LanguageSupported("Norwegian", "no", "Latn; Bokmål"),
//    LanguageSupported("Polish", "pl", "Latn"),
//    LanguageSupported("Portuguese", "pt", "Latn; Brazilian"),
//    LanguageSupported("Romanian", "ro", "Latn"),
//    LanguageSupported("Serbian", "sr-Latn", "Latn"),
//    LanguageSupported("Slovak", "sk", "Latn"),
//    LanguageSupported("Slovenian", "sl", "Latn"),
//    LanguageSupported("Spanish", "es", "Latn; European"),
//    LanguageSupported("Swedish", "sv", "Latn"),
//    LanguageSupported("Turkish", "tr", "Latn"),
//    LanguageSupported("Vietnamese", "vi", "Latn"),
//    // ⚠️ unofficial support
//    LanguageSupported("Bangla", "bn", "Beng; unofficial (partially works via Devanagari model)")
//)

val supportedLanguagesV2 = listOf(
    LanguageSupported("Auto Detect-(latin)", "af", "Latn"),
    LanguageSupported("中文-(Chines)", "zh", "Hans/Hant; supported in v2"),
    LanguageSupported("हिन्दी-(Hindi)", "zh", "Deva"),
    LanguageSupported("日本語-(Japanese)", "ja", "Jpan; supported in v2"),
    LanguageSupported("한국어-(Korean)", "ko", "Kore; supported in v2"),
    LanguageSupported("मराठी-(Marathi)", "mr", "Deva"),
    LanguageSupported("नेपाली-(Nepali)", "ne", "Deva"),
    LanguageSupported("বাংলা-(Bangla)", "bn", "Deva)")
)

data class ScanItem(
    val icon: Int = 0,
    val title: String=""
    )

val scanItems = listOf(
    ScanItem(icon = R.drawable.ic_pdf_img,"Image to pdf"),
    ScanItem(icon = R.drawable.outline_document_scanner_24,"Document to text"),
    ScanItem(icon = R.drawable.identity,"ID card scan"),
    ScanItem(icon = R.drawable.baseline_qr_code_scanner_24,"Bar code scan"),
)