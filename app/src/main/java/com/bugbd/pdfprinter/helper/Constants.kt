package com.bugbd.pdfprinter.helper

object Constants {
    enum class QrCodeType{
        URL,
        TEXT,
        EMAIL,
        CONTACT,
        WIFI,
        PHONE,
        CALENDAR,
        LOCATION,
        WEBSITE,
        DIRECT_LINK,
        SMS,
        TWITTER,
        FACEBOOK,
        YOUTUBE,
        WHATSAPP,
        INSTAGRAM,
        TIKTOK,
        PAYPAL,
        COMMON,
        BAR_CODE,

    }

    var totalPdfCount = 0;
    val firstTimeVisit = "firstTimeVisit"


}