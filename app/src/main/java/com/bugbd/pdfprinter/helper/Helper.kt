package com.bugbd.pdfprinter.helper

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.print.PrintManager
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.FileProvider
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


fun saveTextAsPdf(context: Context, fileName: String, text: String, onSave: (String) -> Unit) {
        val pdfDocument = PdfDocument()

        val pageWidth = 595
        val pageHeight = 842
        val margin = 40f

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL) // বাংলা/Unicode ঠিক রাখতে
        }

        val lineHeight = paint.textSize + 10
        val maxLinesPerPage = ((pageHeight - margin * 2) / lineHeight).toInt()

        val lines = text.split("\n")
        var lineIndex = 0
        var pageNumber = 1

        while (lineIndex < lines.size) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            var y = margin + paint.textSize
            var count = 0

            while (count < maxLinesPerPage && lineIndex < lines.size) {
                canvas.drawText(lines[lineIndex], margin, y, paint)
                y += lineHeight
                lineIndex++
                count++
            }

            pdfDocument.finishPage(page)
            pageNumber++
        }

        // Downloads ফোল্ডারে সেভ করি
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloads.exists()) downloads.mkdirs()

        val file = File(downloads, "$fileName.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file
    )

        Toast.makeText(context, "PDF saved at: $uri", Toast.LENGTH_LONG).show()
    onSave(uri.toString())
}

fun saveTextAsTxt(context: Context, fileName: String, text: String): Uri {
    val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    if (!downloads.exists()) downloads.mkdirs()
    val file = File(downloads, "$fileName.txt")
    file.writeText(text, Charsets.UTF_8)

    // FileProvider দিয়ে Uri বানাই
    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file
    )
    Toast.makeText(context, "PDF saved at: $uri", Toast.LENGTH_LONG).show()
 return  uri
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


fun renamePdfFile(context: Context, uri: Uri, newName: String): File? {
    return if (uri.scheme.equals("file", ignoreCase = true)) {
        // ✅ Case 1: file://
        val originalFile = File(uri.path ?: return null)
        val newFile = File(originalFile.parent, "$newName.pdf")

        if (originalFile.renameTo(newFile)) {
            newFile
        } else null
    } else if (uri.scheme.equals("content", ignoreCase = true)) {
        // ✅ Case 2: content:// (FileProvider / MediaStore)
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: return null

        val newFile = File(context.cacheDir, "$newName.pdf")

        FileOutputStream(newFile).use { output ->
            inputStream.use { input ->
                input.copyTo(output)
            }
        }

        newFile
    } else {
        null
    }
}

fun openPdfInEditor(context: Context, pdfUri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(pdfUri, "application/pdf")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "PDF editor app not found", Toast.LENGTH_SHORT).show()
    }
}

fun printPdf(context: Context, pdfUri: Uri, jobName: String = "PDF Document") {
    try {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager

        val printAdapter = PdfPrinter(context, pdfUri)

        printManager.print(jobName, printAdapter, null)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Print করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
    }
}