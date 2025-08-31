package com.bugbd.pdfprinter.helper

import android.content.Context
import androidx.lifecycle.LiveData
import com.bugbd.pdfprinter.helper.WifiNetwork
import com.bugbd.pdfprinter.local_bd.ScannerDB
import com.bugbd.pdfprinter.local_bd.ScannerDao
import com.bugbd.qrcode.model.ScanFile
import com.bugbd.qrcode.model.ScanModel
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
