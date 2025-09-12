package com.bugbd.pdfprinter.local_bd

import androidx.lifecycle.LiveData
import androidx.room.*
import com.bugbd.qrcode.model.ScanFile

@Dao
interface ScannerDao {
    //scan file
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanFile(scanFile: ScanFile)

    @Update
    suspend fun updateScanFile(scanFile: ScanFile)

    @Delete
    suspend fun deleteFile(scanFile: ScanFile)

    @Query("SELECT * FROM ScanFile order by time desc")
    fun getScanFileList(): LiveData<List<ScanFile>>
}