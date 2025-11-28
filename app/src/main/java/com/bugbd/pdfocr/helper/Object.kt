package com.bugbd.pdfocr.helper

import android.graphics.Bitmap
import android.util.LruCache

object PdfBitmapCache {
    private val memoryCache = LruCache<String, Bitmap>(50) // 50 items cache

    fun put(key: String, bitmap: Bitmap) {
        memoryCache.put(key, bitmap)
    }

    fun get(key: String): Bitmap? {
        return memoryCache.get(key)
    }
}
