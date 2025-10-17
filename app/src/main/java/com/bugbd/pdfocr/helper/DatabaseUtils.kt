package com.bugbd.pdfocr.helper

import android.content.Context
import androidx.lifecycle.LiveData
import com.bugbd.pdfocr.local_bd.ScannerDB
import com.bugbd.pdfocr.local_bd.ScannerDao
import com.bugbd.pdfocr.model.ScanFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoomUtils(private val context: Context) {

    private val db: ScannerDB by lazy {
        ScannerDB.getInstance(context)
    }
    private val scannerDao: ScannerDao by lazy {
        db.scannerDao()
    }

    // Insert an item
    fun insertItem(item: ScanFile) {
        CoroutineScope(Dispatchers.Main).launch {
            scannerDao.insertScanFile(item)
        }
    }
    fun getConnectedWifi():LiveData<List<ScanFile>>{
       return scannerDao.getScanFileList()
    }
}
