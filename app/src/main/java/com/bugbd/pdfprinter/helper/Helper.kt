package com.bugbd.pdfprinter.helper

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bugbd.pdfprinter.databinding.CustomProgressDialogBinding
import com.bugbd.pdfprinter.databinding.CustomProgressDialogLayoutBinding
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


fun saveTextAsPdf(context: Context, fileName: String, text: String) {
    val pdfDocument = PdfDocument()

    // Page size (A4 এর মতো)
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)

    val canvas = page.canvas
    val paint = Paint()
    paint.color = Color.BLACK
    paint.textSize = 16f

    // লাইন বাই লাইন লিখি
    val lines = text.split("\n")
    var y = 50f
    for (line in lines) {
        canvas.drawText(line, 40f, y, paint)
        y += paint.textSize + 10
    }

    pdfDocument.finishPage(page)

    // ফাইল লোকেশনে লিখি
    val file = File(context.getExternalFilesDir(null), "$fileName.pdf")
    pdfDocument.writeTo(FileOutputStream(file))
    pdfDocument.close()

    Toast.makeText(context, "PDF saved at: ${file.absolutePath}", Toast.LENGTH_LONG).show()
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

//progress dialog
fun intiProgressDialog(context: Context, layoutInflater: LayoutInflater): Dialog {
    val progressDialog = Dialog(context)
    progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    val binding = CustomProgressDialogLayoutBinding.inflate(layoutInflater)
    progressDialog.setContentView(binding.root)
    progressDialog.window?.setLayout(120, 120)
    progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    progressDialog.setCancelable(false)

    return progressDialog
}
fun showProgress(progressDialog: Dialog){
    if (!progressDialog.isShowing) {
        progressDialog.show()
    }
}

fun hideProgressDialog(progressDialog: Dialog) {
    if (progressDialog.isShowing) {
        progressDialog.dismiss()
    }
}