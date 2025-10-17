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
import android.os.Build
import android.os.Environment
import android.print.PrintManager
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
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
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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


@Suppress("DEPRECATION")
fun saveTextAsPdf(
    context: Context,
    fileName: String,
    text: String,
    onSave: (String) -> Unit,
    isLoading: (Boolean) -> Unit
) {
    isLoading(true)
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val pdfDocument = PdfDocument()

            val pageWidth = 595
            val pageHeight = 842
            val marginTop = 40f
            val marginBottom = 60f   // bottom একটু বেশি রাখো (print safe)
            val marginLeft = 40f
            val marginRight = 40f

            val paint = TextPaint().apply {
                color = Color.BLACK
                textSize = 12f // 🔹 smaller text size for better fit
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }

            val lineHeight = paint.textSize + 8f
            val availableWidth = pageWidth - (marginLeft + marginRight)
            val availableHeight = pageHeight - (marginTop + marginBottom)

            var remainingText = text
            var pageNumber = 1

            while (remainingText.isNotEmpty()) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    StaticLayout.Builder
                        .obtain(remainingText, 0, remainingText.length, paint, availableWidth.toInt())
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(6f, 1f)
                        .setIncludePad(false)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    StaticLayout(
                        remainingText, paint, availableWidth.toInt(),
                        Layout.Alignment.ALIGN_NORMAL, 1f, 6f, false
                    )
                }

                canvas.save()
                canvas.translate(marginLeft, marginTop)

                // ✅ bottom margin respect করা (visible অংশ পর্যন্ত আঁকা)
                var lastLineIndex = 0
                for (i in 0 until staticLayout.lineCount) {
                    val lineBottom = staticLayout.getLineBottom(i)
                    if (lineBottom > availableHeight - 10f) { // কিছু padding রাখো
                        lastLineIndex = i
                        break
                    }
                    lastLineIndex = i
                }

                // শুধু ফিট করা অংশ আঁকতে canvas.clipRect ব্যবহার
                canvas.clipRect(
                    0f,
                    0f,
                    availableWidth,
                    availableHeight - 10f
                )

                staticLayout.draw(canvas)
                canvas.restore()

                if (staticLayout.height <= availableHeight) {
                    remainingText = ""
                } else {
                    val lastLineEnd = staticLayout.getLineEnd(lastLineIndex)
                    remainingText = remainingText.substring(lastLineEnd).trimStart()
                }

                pdfDocument.finishPage(page)
                pageNumber++
            }

            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloads.exists()) downloads.mkdirs()

            val file = File(downloads, "$fileName.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            withContext(Dispatchers.Main) {
                isLoading(false)
                Toast.makeText(context, "PDF generated successfully:\n$uri", Toast.LENGTH_LONG).show()
                onSave(uri.toString())
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                isLoading(false)
                Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
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

fun getBarCodeFormat(type: Int, barcode: Barcode): String {
    // Types that often contain plain text but you want to return barcode format instead:
    val textLikeTypes = setOf(
        Barcode.TYPE_TEXT,
        Barcode.TYPE_PRODUCT,
        Barcode.TYPE_ISBN
    )

    if (type in textLikeTypes) {
        // Return format name instead of "Text" or "Product" etc
        return when (barcode.format) {
            Barcode.FORMAT_QR_CODE -> "QR_CODE"
            Barcode.FORMAT_CODE_128 -> "CODE_128"
            Barcode.FORMAT_CODE_39 -> "CODE_39"
            Barcode.FORMAT_CODE_93 -> "CODE_93"
            Barcode.FORMAT_CODABAR -> "CODABAR"
            Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
            Barcode.FORMAT_EAN_13 -> "EAN_13"
            Barcode.FORMAT_EAN_8 -> "EAN_8"
            Barcode.FORMAT_ITF -> "ITF"
            Barcode.FORMAT_PDF417 -> "PDF417"
            Barcode.FORMAT_AZTEC -> "AZTEC"
            else -> "UNKNOWN_FORMAT"
        }
    }

    return when (type) {
        Barcode.TYPE_CONTACT_INFO -> "Contact Info"
        Barcode.TYPE_EMAIL -> "Email"
        Barcode.TYPE_PHONE -> "Phone"
        Barcode.TYPE_SMS -> "SMS"
        Barcode.TYPE_URL -> "URL"
        Barcode.TYPE_GEO -> "Geo Location"
        Barcode.TYPE_CALENDAR_EVENT -> "Calendar Event"
        Barcode.TYPE_DRIVER_LICENSE -> "Driver License"
        else -> {
            // If unknown type and not textLike, fallback to format name:
            when (barcode.format) {
                Barcode.FORMAT_QR_CODE -> "QR_CODE"
                Barcode.FORMAT_CODE_128 -> "CODE_128"
                Barcode.FORMAT_CODE_39 -> "CODE_39"
                Barcode.FORMAT_CODE_93 -> "CODE_93"
                Barcode.FORMAT_CODABAR -> "CODABAR"
                Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
                Barcode.FORMAT_EAN_13 -> "EAN_13"
                Barcode.FORMAT_EAN_8 -> "EAN_8"
                Barcode.FORMAT_ITF -> "ITF"
                Barcode.FORMAT_PDF417 -> "PDF417"
                Barcode.FORMAT_AZTEC -> "AZTEC"
                else -> "UNKNOWN_FORMAT"
            }
        }
    }
}
fun getBarcodeResult(type: Int, barcode: Barcode): String {
    return when (type) {
        Barcode.TYPE_CONTACT_INFO -> {
            val contact = barcode.contactInfo
            val name = contact?.name?.formattedName ?: ""
            val organization = contact?.organization ?: ""
            val phones = contact?.phones?.joinToString { it.number ?: "" } ?: ""
            val emails = contact?.emails?.joinToString { it.address ?: "" } ?: ""
            "Contact: $name, Org: $organization, Phones: $phones, Emails: $emails"
        }
        Barcode.TYPE_EMAIL -> {
            val email = barcode.email
            "Email: ${email?.address ?: ""}, Subject: ${email?.subject ?: ""}, Body: ${email?.body ?: ""}"
        }
        Barcode.TYPE_ISBN -> {
            "ISBN: ${barcode.rawValue ?: ""}"
        }
        Barcode.TYPE_PHONE -> {
            val phone = barcode.phone
            "Phone: ${phone?.number ?: ""}, Type: ${phone?.type ?: ""}"
        }
        Barcode.TYPE_PRODUCT -> {
            "Product Code: ${barcode.rawValue ?: ""}"
        }
        Barcode.TYPE_SMS -> {
            val sms = barcode.sms
            "SMS to: ${sms?.phoneNumber ?: ""}, Message: ${sms?.message ?: ""}"
        }
        Barcode.TYPE_TEXT -> {
            "Text: ${barcode.rawValue ?: ""}"
        }
        Barcode.TYPE_URL -> {
            val url = barcode.url
            "URL: ${url?.url ?: ""}, Title: ${url?.title ?: ""}"
        }
        Barcode.TYPE_WIFI -> {
            val wifi = barcode.wifi
            "Wi-Fi SSID: ${wifi?.ssid ?: ""}, Password: ${wifi?.password ?: ""}, Encryption: ${wifi?.encryptionType ?: ""}"
        }
        Barcode.TYPE_GEO -> {
            val geo = barcode.geoPoint
            "Location: ${geo?.lat ?: ""}, ${geo?.lng ?: ""}"
        }
        Barcode.TYPE_CALENDAR_EVENT -> {
            val event = barcode.calendarEvent
            "Event: ${event?.summary ?: ""}, Start: ${event?.start?.rawValue ?: ""}, End: ${event?.end?.rawValue ?: ""}"
        }
        Barcode.TYPE_DRIVER_LICENSE -> {
            val dl = barcode.driverLicense
            "DL Name: ${dl?.firstName ?: ""} ${dl?.lastName ?: ""}, Number: ${dl?.licenseNumber ?: ""}"
        }
        else -> {
            "Other type: ${barcode.rawValue ?: ""}"
        }
    }
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