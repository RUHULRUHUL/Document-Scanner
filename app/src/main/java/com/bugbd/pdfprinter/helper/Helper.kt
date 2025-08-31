package com.bugbd.pdfprinter.helper

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.bugbd.pdfprinter.helper.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream


fun convertImageToBase64(bitmap: Bitmap?): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

fun decodeBase64ToImage(base64String: String): Bitmap? {
    return try {
        val decodedBytes: ByteArray = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        null
    }
}

fun String.logD(tag: String = "") {
    Log.d("LogPrintPdfPrinter $tag", this)
}



//fun imageSave(context: Context, bitmap: Bitmap?,callback: (status: Response)->Unit) {
//    val alertDialogBuilder = AlertDialog.Builder(context)
//    val imageName = EditText(context)
//    imageName.hint = context.getString(R.string.input_image_name)
//    alertDialogBuilder.setTitle(context.getString(R.string.qr_bar_code))
//    alertDialogBuilder
//        .setMessage(context.getString(R.string.are_you_sure_you_want_to_save_phone_memory))
//        .setView(imageName)
//        .setPositiveButton("OK") { _: DialogInterface?, id: Int ->
//            try {
//                val filename = imageName.text.toString()
//                val file: File? = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//                    ?: context.filesDir
//                val derectory = File(file, "rizQRScan")
//                if (!derectory.exists()) {
//                    derectory.mkdir()
//                    Log.d("ImageSave", "File not found - that why file create $derectory")
//                }
//
//                derectory.let {
//                    val outPutFile =
//                        File(derectory, filename + "_" + System.currentTimeMillis() + ".jpg")
//                    try {
//                        val fileOutputStream = FileOutputStream(outPutFile)
//                        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream!!)
//                        fileOutputStream!!.flush()
//                        fileOutputStream!!.close()
//                        Log.d("ImageSave","image save gallery $outPutFile")
//                        MediaScannerConnection.scanFile(context, arrayOf(outPutFile.toString()), null
//                        ) { path: String, uri: Uri? ->
//                            callback(
//                                Response(
//                                    status = true,
//                                    message = context.getString(R.string.qr_code_generate_successfully)
//                                )
//                            )
//                            Log.d("SaveImage", "Image saved to gallery: $path")
//                        }
//
//
//                    } catch (e: FileNotFoundException) {
//
//                        Log.d("ImageSave","error ${e.localizedMessage}")
//                    }
//                }
//            }catch (e:Exception){
//                e.printStackTrace()
//            }
//        }
//        .setNegativeButton("CANCEL") { dialog: DialogInterface, id: Int -> dialog.cancel() }
//    val alertDialog = alertDialogBuilder.create()
//    alertDialog.setCanceledOnTouchOutside(false)
//    alertDialog.show()
//}
