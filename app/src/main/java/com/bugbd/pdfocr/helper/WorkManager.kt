//package com.bugbd.qrcode.helper
//
//import android.content.Context
//import android.content.Intent
//import androidx.work.Worker
//import androidx.work.WorkerParameters
//
//class WorkManagerWidget(appContext: Context, workerParams: WorkerParameters):
//    Worker(appContext, workerParams) {
//    override fun doWork(): Result {
//        val intent = Intent("com.bugbd.docfilescanner.WIDGET_UPDATE")
//        applicationContext.sendBroadcast(intent)
//        return Result.success()
//    }
//}
