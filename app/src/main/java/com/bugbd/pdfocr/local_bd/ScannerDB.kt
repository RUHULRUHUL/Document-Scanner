package com.bugbd.pdfocr.local_bd

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bugbd.pdfocr.helper.WifiNetwork
import com.bugbd.pdfocr.model.ScanFile
import com.bugbd.pdfocr.model.ScanHistory
import com.bugbd.pdfocr.model.ScanModel

@Database(
    entities = [
        ScanModel::class,
        ScanFile::class,
    WifiNetwork::class,
        ScanHistory::class
    ],
    version = 5,
    exportSchema = false
)
abstract class ScannerDB : RoomDatabase() {

    abstract fun scannerDao(): ScannerDao

    companion object {
        @Volatile
        var instance: ScannerDB? = null

        fun getInstance(context: Context): ScannerDB {
            if (instance == null) {
                synchronized(this) {
                    instance = Room.databaseBuilder(
                        context,
                        ScannerDB::class.java,
                        "com.bugbd.docfilescanner-room"
                    )
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build()
                }
            }

            return instance!!
        }
    }


}