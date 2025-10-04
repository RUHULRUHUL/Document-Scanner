package com.bugbd.pdfprinter.helper

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.location.LocationManager
import android.location.LocationRequest
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.format.DateUtils
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import coil.load
import coil.transform.RoundedCornersTransformation
import com.bugbd.pdfprinter.R
import com.bugbd.pdfprinter.databinding.CustomProgressDialogBinding
//import com.bugbd.qrcode.R
//import com.bugbd.qrcode.databinding.CustomProgressDialogBinding
import com.bugbd.qrcode.model.LanguageItem
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import com.google.android.gms.location.LocationRequest
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.location.LocationSettingsRequest
//import com.google.android.gms.location.LocationSettingsResponse
//import com.google.android.gms.location.LocationSettingsStatusCodes
//import com.google.android.gms.tasks.Task
//import com.google.android.gms.vision.barcode.Barcode
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Objects
import androidx.core.graphics.createBitmap

class Utils {
    companion object {

        val logTag_Doc = "DocScanner"
        val logTag_QR = "QRScanner"
        val QR_TXT = "qrTxt"
        val FILE_TXT = "file"
        val channelID = "com.bugbd.docfilescanner"


        fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }


        fun showTransparentProgressDialog(context: Context, id: Int?): Dialog {
            val progressDialog = Dialog(context)
            id?.let {
                val view: View = LayoutInflater.from(context).inflate(id, null)
                progressDialog.setContentView(view)
                progressDialog.window?.setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                //progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                progressDialog.setCancelable(false)
                progressDialog.setCanceledOnTouchOutside(false)
            }
            return progressDialog
        }

        fun Dialog.showDialog() {
            this.show()

        }

        fun Dialog.dismissDialog() {
            this.dismiss()
        }

//        fun getFormat(format:Int):String{
//            var format = ""
//            if (barcode.valueAt(0).valueFormat == Barcode.WIFI){
//                format = "wifi"
//
//            }
//            return format
//        }

//        fun getTypeOfFormat(barcode: Int): String {
//            return when (barcode) {
//                Barcode.QR_CODE -> "QR Code"
//                else -> "Bar Code"
//            }
//        }

//        fun getFormatDetailsName(barcode: Int): String {
//            return when (barcode) {
////                Barcode.CODE_128 -> "CODE_128"
////                Barcode.CODE_39 -> "CODE_39"
////                Barcode.CODE_93 -> "CODE_93"
////                Barcode.CODABAR -> "CODABAR"
//                Barcode.DATA_MATRIX -> "DATA_MATRIX"
//                Barcode.EAN_13 -> "EAN_13"
//                Barcode.EAN_8 -> "EAN_8"
//                Barcode.UPC_A -> "UPC_A"
//                Barcode.UPC_E -> "UPC_E"
//                Barcode.PDF417 -> "PDF417"
//                Barcode.AZTEC -> "AZTEC"
//                Barcode.CONTACT_INFO -> "CONTACT INFO"
//                Barcode.EMAIL -> "EMAIL"
//                Barcode.ISBN -> "ISBN"
//                Barcode.PHONE -> "PHONE"
//                Barcode.PRODUCT -> "PRODUCT"
//                Barcode.SMS -> "SMS"
//                Barcode.TEXT -> "TEXT"
//                Barcode.URL -> "URL"
//                Barcode.WIFI -> "WIFI"
//                Barcode.GEO -> "GEO"
//                Barcode.CALENDAR_EVENT -> "CALENDAR_EVENT"
//                Barcode.DRIVER_LICENSE -> "DRIVER_LICENSE"
//                else -> "Qr code"
//            }
//        }

//        fun getTypeOfFormatRawText(barcode: Int, detect: SparseArray<Barcode>): String {
//            val stringBuilder = StringBuilder()
//            return when (barcode) {
//                Barcode.WIFI -> {
//                    stringBuilder.append("WIFI QR Code")
//                    newLine(stringBuilder)
//                    stringBuilder.append("raw format:")
//                    newLine(stringBuilder)
//                    stringBuilder.append("raw format: ${detect.valueAt(0).rawValue}")
//                    newLine(stringBuilder)
//                    newLine(stringBuilder)
//                    stringBuilder.append("name: ${detect.valueAt(0)?.wifi?.ssid ?: ""}")
//                    newLine(stringBuilder)
//                    stringBuilder.append("password: ${detect.valueAt(0)?.wifi?.password ?: ""}")
//                    newLine(stringBuilder)
//
//                    when (detect.valueAt(0)?.wifi?.encryptionType) {
//                        1 -> {
//                            stringBuilder.append("encryption type: Open ")
//                        }
//
//                        2 -> {
//                            stringBuilder.append("encryption type: WPA ")
//                        }
//
//                        3 -> {
//                            stringBuilder.append("encryption type: NONE ")
//                        }
//
//                        else -> {
//                            stringBuilder.append("encryption type: NONE ")
//                        }
//                    }
//                    newLine(stringBuilder)
//                    return stringBuilder.toString()
//                }
//
//                Barcode.CONTACT_INFO -> {
//                    stringBuilder.append("CONTACT INFO")
//                    newLine(stringBuilder)
//                    stringBuilder.append("raw format:")
//                    newLine(stringBuilder)
//                    stringBuilder.append("raw format: ${detect.valueAt(0).rawValue ?: ""}")
//                    newLine(stringBuilder)
//                    newLine(stringBuilder)
//                    stringBuilder.append("name: ${detect.valueAt(0)?.contactInfo?.name?.formattedName ?: ""}")
//                    newLine(stringBuilder)
//
//                    stringBuilder.append("organization: ${detect.valueAt(0)?.contactInfo?.organization ?: ""}")
//                    newLine(stringBuilder)
//
//                    detect.valueAt(0)?.contactInfo?.phones?.forEach { phone ->
//                        stringBuilder.append("Phone: ${phone?.number ?: ""}\n")
//                    }
//                    newLine(stringBuilder)
//
//                    detect.valueAt(0)?.contactInfo?.emails?.forEach { email ->
//                        stringBuilder.append("Email: ${email?.address ?: ""} \n")
//                    }
//                    newLine(stringBuilder)
//
//                    detect.valueAt(0)?.contactInfo?.addresses?.forEach { address ->
//                        stringBuilder.append("Address: ${address?.addressLines?.joinToString(", ")}\n")
//                    }
//
//                    detect.valueAt(0)?.contactInfo?.urls?.forEach { url ->
//                        stringBuilder.append("url: ${url ?: ""}\n")
//                    }
//                    newLine(stringBuilder)
//                    return stringBuilder.toString()
//                }
//
//                Barcode.EMAIL -> {
//                    stringBuilder.append("Email\n")
//                    newLine(stringBuilder)
//                    stringBuilder.append("raw format:")
//                    newLine(stringBuilder)
//                    stringBuilder.append("raw format: ${detect.valueAt(0).rawValue ?: ""}")
//                    newLine(stringBuilder)
//                    newLine(stringBuilder)
//                    stringBuilder.append("Address: ${detect.valueAt(0)?.email?.address ?: ""}\n")
//                    newLine(stringBuilder)
//                    stringBuilder.append("Subject: ${detect.valueAt(0)?.email?.subject ?: ""}\n")
//                    newLine(stringBuilder)
//                    stringBuilder.append("Body: ${detect.valueAt(0)?.email?.body ?: ""}\n")
//                    newLine(stringBuilder)
//
//                    return stringBuilder.toString()
//                }
//
//                Barcode.PHONE -> {
//                    stringBuilder.append("Phone\n")
//                    newLine(stringBuilder)
//                    stringBuilder.append("raw format:")
//                    newLine(stringBuilder)
//                    stringBuilder.append("raw format: ${detect.valueAt(0).rawValue ?: ""}")
//                    newLine(stringBuilder)
//                    newLine(stringBuilder)
//                    stringBuilder.append("Number: ${detect.valueAt(0)?.phone?.number ?: ""}\n")
//                    newLine(stringBuilder)
//                    return stringBuilder.toString()
//                }
//
//                Barcode.SMS -> {
//                    stringBuilder.append("SMS\n")
//                    newLine(stringBuilder)
//                    stringBuilder.append("raw format:")
//                    newLine(stringBuilder)
//                    stringBuilder.append("raw format: ${detect.valueAt(0).rawValue ?: ""}")
//                    newLine(stringBuilder)
//                    newLine(stringBuilder)
//                    stringBuilder.append("Number: ${detect.valueAt(0)?.sms?.phoneNumber ?: ""}\n")
//                    newLine(stringBuilder)
//                    stringBuilder.append("Message: ${detect.valueAt(0)?.sms?.message ?: ""}\n")
//                    newLine(stringBuilder)
//                    return stringBuilder.toString()
//                }
//
//                Barcode.GEO -> {
//                    stringBuilder.append("Geo Point\n")
//                    newLine(stringBuilder)
//                    stringBuilder.append("raw format:")
//                    newLine(stringBuilder)
//                    stringBuilder.append("raw format: ${detect.valueAt(0).rawValue ?: ""}")
//                    newLine(stringBuilder)
//                    newLine(stringBuilder)
//                    stringBuilder.append("Latitude: ${detect.valueAt(0)?.geoPoint?.lat ?: ""}\n")
//                    newLine(stringBuilder)
//                    stringBuilder.append("Longitude: ${detect.valueAt(0)?.geoPoint?.lng ?: ""}\n")
//                    newLine(stringBuilder)
//                    return stringBuilder.toString()
//                }
//
//                Barcode.CALENDAR_EVENT -> {
//                    stringBuilder.append("Calendar Event\n")
//                    newLine(stringBuilder)
//                    stringBuilder.append("raw format:")
//                    newLine(stringBuilder)
//                    stringBuilder.append("raw format: ${detect.valueAt(0).rawValue ?: ""}")
//                    newLine(stringBuilder)
//                    newLine(stringBuilder)
//
//                    stringBuilder.append("Summary: ${detect.valueAt(0)?.calendarEvent?.summary ?: ""}\n")
//                    newLine(stringBuilder)
//                    stringBuilder.append("Description: ${detect.valueAt(0)?.calendarEvent?.description ?: ""}\n")
//                    newLine(stringBuilder)
//
//                    stringBuilder.append("organizer: ${detect.valueAt(0)?.calendarEvent?.organizer ?: ""}\n")
//                    newLine(stringBuilder)
//
//                    stringBuilder.append("Geo Location: ${detect.valueAt(0)?.calendarEvent?.location ?: ""}\n")
//                    newLine(stringBuilder)
//
//                    stringBuilder.append(
//                        "Start date: ${detect.valueAt(0)?.calendarEvent?.start?.day ?: ""}-${
//                            detect.valueAt(
//                                0
//                            )?.calendarEvent?.start?.month ?: ""
//                        }-${detect.valueAt(0)?.calendarEvent?.start?.year ?: ""}  ${detect.valueAt(0)?.calendarEvent?.start?.hours ?: ""}:${
//                            detect.valueAt(
//                                0
//                            )?.calendarEvent?.start?.minutes ?: ""
//                        }\n"
//                    )
//                    newLine(stringBuilder)
//
//
//                    stringBuilder.append(
//                        "End date: ${detect.valueAt(0)?.calendarEvent?.end?.day ?: ""}-${
//                            detect.valueAt(
//                                0
//                            )?.calendarEvent?.end?.month ?: ""
//                        }-${detect.valueAt(0)?.calendarEvent?.end?.year ?: ""}  ${detect.valueAt(0)?.calendarEvent?.end?.hours ?: ""}:${
//                            detect.valueAt(
//                                0
//                            )?.calendarEvent?.end?.minutes ?: ""
//                        }\n"
//                    )
//                    newLine(stringBuilder)
//
//                    return stringBuilder.toString()
//                }
//
//                Barcode.DRIVER_LICENSE -> {
//                    stringBuilder.append("Driver License\n")
//                    newLine(stringBuilder)
//                    stringBuilder.append("License Number: ${detect.valueAt(0)?.driverLicense?.licenseNumber ?: ""}\n")
//                    newLine(stringBuilder)
//                    stringBuilder.append("First Name: ${detect.valueAt(0)?.driverLicense?.firstName ?: ""}\n")
//                    newLine(stringBuilder)
//
//                    stringBuilder.append("Last Name: ${detect.valueAt(0)?.driverLicense?.lastName ?: ""}\n")
//                    newLine(stringBuilder)
//
//                    stringBuilder.append("Gender: ${detect.valueAt(0)?.driverLicense?.gender ?: ""}\n")
//                    newLine(stringBuilder)
//
//                    stringBuilder.append(
//                        "Address: ${detect.valueAt(0)?.driverLicense?.addressStreet ?: ""}," +
//                                " ${detect.valueAt(0)?.driverLicense?.addressCity ?: ""}, " +
//                                "${detect.valueAt(0)?.driverLicense?.addressState ?: ""} ${
//                                    detect.valueAt(
//                                        0
//                                    )?.driverLicense?.addressZip ?: ""
//                                }\n"
//                    )
//                    newLine(stringBuilder)
//
//                    stringBuilder.append("Birth Date: ${detect.valueAt(0)?.driverLicense?.birthDate ?: ""}\n")
//                    newLine(stringBuilder)
//
//                    stringBuilder.append("Issue Date: ${detect.valueAt(0)?.driverLicense?.issueDate ?: ""}\n")
//                    newLine(stringBuilder)
//
//                    stringBuilder.append("Expiration Date: ${detect.valueAt(0)?.driverLicense?.expiryDate ?: ""}\n")
//                    newLine(stringBuilder)
//
//                    stringBuilder.append("Issuing Country: ${detect.valueAt(0)?.driverLicense?.issuingCountry ?: ""}\n")
//                    newLine(stringBuilder)
//
//                    stringBuilder.append("Document type: ${detect.valueAt(0)?.driverLicense?.documentType ?: ""}\n")
//                    newLine(stringBuilder)
//
//
//                    return stringBuilder.toString()
//                }
//
//                else -> {
//                    detect.valueAt(0).rawValue.toString()
//                }
//            }
//        }

        private fun newLine(stringBuilder: StringBuilder) {
            stringBuilder.append("\n\n")
        }


        fun showLog(log: String, message: String) {
            Log.d(log, message)
        }

        fun getCurrentTimeMills(): Long {
            val c = Calendar.getInstance()
            return c.timeInMillis
        }


        private fun getDateTime(timeStamp: Long): String {
            var date = ""
            try {
                val dateFormat = SimpleDateFormat("dd MMM yy hh:mm a", Locale.getDefault())
                date = dateFormat.format(timeStamp)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return date
        }

        private fun getTime(timeStamp: Long): String {
            var date = ""
            try {
                val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                date = dateFormat.format(timeStamp)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return date
        }

        fun timeAgo(millis: Long): String {
            val diff = Date().time - millis
            val seconds = (kotlin.math.abs(diff) / 1000).toDouble()
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            //  val years = days / 365

            val timeFormat =
                if (seconds < 5 && DateUtils.isToday(millis)) {
                    "just now"
                } else if (seconds < 60 && DateUtils.isToday(millis)) {
                    "${seconds.toInt()} sec"
                } else if (minutes < 60 && DateUtils.isToday(millis)) {
                    "${minutes.toInt()} min"
                } else if (hours < 24 && DateUtils.isToday(millis)) {
                    getTime(millis)
                } else if (!DateUtils.isToday(millis) && hours < 48) {
                    "yesterday"
                } else {
                    getDateTime(millis)
                }.toString()
            return timeFormat
        }

        fun notificationPermissionCheck(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                return true
            }
        }

        fun cameraPermissionCheck(context: Context): Boolean {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

        }

        fun writeStoragePermission(context: Context): Boolean {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun readWritePermission(): Array<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                )
            } else arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        }

        fun permissions(): Array<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.CAMERA
                )
            } else arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        }


        fun hasPermissions(permissions: Array<String>?, activity: Activity): Boolean {
            if (permissions != null) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(
                            activity, permission
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return false
                    }
                }
            }
            return true
        }


        fun readStoragePermission(context: Context): Boolean {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun requestForUserPermission(
            activity: Activity,
            requestCode: Int,
            permission: Array<String>
        ) {
            requestPermissions(activity, permission, requestCode)
        }

        fun checkGpsLocationEnable(context: Context): Boolean {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

        /*
                fun turnOnLocationDialogue(context: Context?) {
                    val locationRequest = LocationRequest.create()
                    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    locationRequest.interval = 5000
                    locationRequest.fastestInterval = 2000
                    val builder = LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest)
                    builder.setAlwaysShow(true)
                    val result = LocationServices.getSettingsClient(
                        context!!
                    )
                        .checkLocationSettings(builder.build())
                    result.addOnCompleteListener { task: Task<LocationSettingsResponse?> ->
                        try {
                            val response =
                                task.getResult(ApiException::class.java)
                        } catch (e: ApiException) {
                            when (e.statusCode) {
                                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                                    val resolvableApiException = e as ResolvableApiException
                                    resolvableApiException.startResolutionForResult(
                                        (context as Activity?)!!,
                                        100
                                    )
                                } catch (ex: IntentSender.SendIntentException) {
                                    ex.printStackTrace()
                                }
                                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                            }
                        }
                    }
                }
        */

        fun wifiConnect(
            context: Context,
            password: String = "",
            ssid: String = ""
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
                    .setSsid(ssid)
                    .setWpa2Passphrase(password)
                    .build()

                val networkRequest = NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .setNetworkSpecifier(wifiNetworkSpecifier)
                    .build()

                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                connectivityManager.requestNetwork(
                    networkRequest,
                    object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: android.net.Network) {
                            // Network is available
                            connectivityManager.bindProcessToNetwork(network)
                        }

                        override fun onLost(network: android.net.Network) {
                            // Network connection lost
                        }
                    })
            } else {
                // Handle for Android versions below Q
            }
        }


        fun locationPermissionCheck(context: Context?): Boolean {
            return ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }

//        fun showRequestPermissionDialog(context: Context, layoutInflater: LayoutInflater) {
//            val progressDialog = ProgressDialog(context)
//            progressDialog.setCanceledOnTouchOutside(false)
//            progressDialog.show()
//            val binding: NotificationPermissionLayoutBinding =
//                NotificationPermissionLayoutBinding.inflate(
//                    layoutInflater
//                )
//            progressDialog.setContentView(binding.root)
//            progressDialog.window!!.setBackgroundDrawableResource(R.color.transparent)
//            binding.allowPermissionBtn.setOnClickListener {
//                progressDialog.dismiss()
//                val intent = Intent()
//                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                val uri = Uri.fromParts("package", context.packageName, null)
//                intent.data = uri
//                context.startActivity(intent)
//            }
//            binding.closeDialogImageView.setOnClickListener { progressDialog.dismiss() }
//        }


        fun intiProgressDialog(context: Context, layoutInflater: LayoutInflater): Dialog {
            val progressDialog = Dialog(context)
            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val binding = CustomProgressDialogBinding.inflate(layoutInflater)
            progressDialog.setContentView(binding.root)
            progressDialog.window?.setLayout(120, 120)
            progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            progressDialog.setCancelable(false)

            return progressDialog
        }

        fun showProgress(progressDialog: Dialog) {
            if (!progressDialog.isShowing) {
                progressDialog.show()
            }
        }

        fun hideProgressDialog(progressDialog: Dialog) {
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }

        fun shareFile(context: Context, shareTitle: String, uri: String) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri.toUri())
                type = "application/pdf"
            }
            context.startActivity(Intent.createChooser(shareIntent, shareTitle))
        }

        fun customAlert(
            context: Context,
            title: String,
            message: String,
            positive: (inputName: String) -> Unit
        ) {
            val alertDialogBuilder = AlertDialog.Builder(context)
            val imageName = EditText(context)
            imageName.hint = "enter name"
            alertDialogBuilder.setTitle(title)
            alertDialogBuilder
                .setMessage(message)
                .setCancelable(true)
                .setView(imageName)
                .setPositiveButton("OK") { _: DialogInterface?, id: Int ->
                    val filename = imageName.text.toString().trim() + ".pdf"
                    positive(filename)
                }
                .setNegativeButton("CANCEL") { dialog: DialogInterface, id: Int ->
                    dialog.cancel()
                    val filename = System.currentTimeMillis().toString().trim() + ".pdf"
                    positive(filename)
                }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(context, R.color.black))
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(context, R.color.black))

        }

        fun showRenameDialog(context: Context,oldFileName:String,onRename: (String) -> Unit) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.rename_input_field, null)

            val editText = dialogView.findViewById<TextInputEditText>(R.id.editFileName)
            val nameWithoutExt = oldFileName.removeSuffix(".pdf")
            editText.setText(nameWithoutExt)

            val dialog = AlertDialog.Builder(context)
                .setTitle("Rename File")
                .setView(dialogView)
                .setPositiveButton("OK") { d, _ ->
                    val newName = editText.text.toString().trim()
                    if (newName.isNotEmpty()) {
                        onRename(newName)
                    } else {
                        Utils.showToast(context, "File name cannot be empty")
                    }
                    d.dismiss()
                }
                .setNegativeButton("Cancel") { d, _ ->
                    d.dismiss()
                }
                .create()

            dialog.show()
        }

        /*fun customAlert(
            context: Context,
            title: String,
            message: String,
            positive: (inputName: String) -> Unit
        ) {
            val alertDialogBuilder = AlertDialog.Builder(context)
            val imageName = EditText(context)
            imageName.hint = "enter name"
            alertDialogBuilder.setTitle(title)
            alertDialogBuilder
                .setMessage(message)
                .setCancelable(true)
                .setView(imageName)
                .setPositiveButton("OK") { _: DialogInterface?, id: Int ->
                    showLog(logTag_Doc, "setPositiveButton click ok")
                    val filename = imageName.text.toString()
                    positive(filename)
                }
                .setNegativeButton("CANCEL") { dialog: DialogInterface, id: Int -> dialog.cancel() }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(context, R.color.black))
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(context, R.color.black))

        }*/

        /* fun exitDialog(
             context: Context,
             title: String,
             message: String,
             positive: (inputName: String) -> Unit
         ) {
             val alertDialogBuilder = AlertDialog.Builder(context)
             alertDialogBuilder.setTitle(title)
             alertDialogBuilder
                 .setMessage(message)
                 .setCancelable(true)
                 .setPositiveButton("OK") { _: DialogInterface?, id: Int ->
                     showLog(logTag_Doc, "setPositiveButton click ok")
                     positive("ok")
                 }
                 .setNegativeButton("CANCEL") { dialog: DialogInterface, id: Int -> dialog.cancel() }
             val alertDialog = alertDialogBuilder.create()
             alertDialog.show()
             alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                 .setTextColor(ContextCompat.getColor(context, R.color.black))
             alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                 .setTextColor(ContextCompat.getColor(context, R.color.black))

         }*/


        fun getPdfFilesFromDownloadFolder(context: Context): List<PdfFileItem> {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val pdfFiles = downloadDir.listFiles { file -> file.extension.equals("pdf", ignoreCase = true) } ?: return emptyList()

            return pdfFiles.mapNotNull { file ->
                try {
                    val uri = Uri.fromFile(file)
                    val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    val pdfRenderer = PdfRenderer(pfd)
                    val pageCount = pdfRenderer.pageCount
                    pdfRenderer.close()
                    pfd.close()

                    PdfFileItem(
                        uri = uri,
                        name = file.name,
                        sizeInBytes = file.length(),
                        pageCount = pageCount,
                        createdTimeMillis = file.lastModified()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null // Skip file if an error occurs
                }
            }
        }

        fun getPdfFilesFromDownloads(context: Context): List<PdfFileItem> {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            if (!downloadDir.exists() || !downloadDir.isDirectory) return emptyList()

            val pdfFiles = downloadDir.listFiles { file ->
                file.extension.equals("pdf", ignoreCase = true)
            } ?: return emptyList()

            val pdfFileItems = mutableListOf<PdfFileItem>()

            for (file in pdfFiles) {
                try {
                    val uri = Uri.fromFile(file)
                    val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(pfd)
                    val pageCount = renderer.pageCount
                    renderer.close()
                    pfd.close()

                    val item = PdfFileItem(
                        uri = uri,
                        name = file.name,
                        sizeInBytes = file.length(),
                        pageCount = pageCount,
                        createdTimeMillis = file.lastModified()
                    )
                    pdfFileItems.add(item)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // skip this file if error
                }
            }

            return pdfFileItems
        }



        fun getPdfFiles(context: Context): List<File> {
            val pdfList = mutableListOf<File>()
            val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
            val selection = MediaStore.Files.FileColumns.MIME_TYPE + "=?"
            val selectionArgs = arrayOf("application/pdf")

            val cursor = context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null
            )

            cursor?.use {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                while (it.moveToNext()) {
                    val filePath = it.getString(columnIndex)
                    val file = File(filePath)
                    if (file.exists()) {
                        pdfList.add(file)
                    }
                }
            }

            return pdfList
        }

        /*private fun pdfSave(
            context: Context,
            document: PdfDocument,
            completeTask: (fileName: String) -> Unit
        ) {
            val alertDialogBuilder = AlertDialog.Builder(context)
            val imageName = EditText(context)
            imageName.hint = "input image name.."
            alertDialogBuilder.setTitle("Qr_Bar code")
            alertDialogBuilder
                .setMessage("Are you sure you want to save phone memory :")
                .setCancelable(true)
                .setView(imageName)
                .setPositiveButton("OK") { _: DialogInterface?, id: Int ->
                    showLog(logTag_Doc, "setPositiveButton click ok")
                    val filename = imageName.text.toString()
                    val file: File? =
                        if (context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) != null) {
                            showLog(logTag_Doc, "check DIRECTORY_DCIM not null")
                            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        } else {
                            showLog(
                                logTag_Doc,
                                "IRECTORY_DCIM null -that why  filesDir ${context.filesDir}"
                            )
                            context.filesDir
                        }
                    val derectory = File(file, "com.bugbd.docScanner")
                    if (!derectory.exists()) {
                        showLog(logTag_Doc, "Derectory not exist now create derectory")
                        derectory.mkdir()
                    }

                    derectory.let {
                        val displayFileName = filename + "_" + System.currentTimeMillis() + ".pdf"
                        val myContentValues = ContentValues()
                        myContentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayFileName)
                        myContentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, it.toString())
                        myContentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        myContentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)
                        val extVolumeUri =
                            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
                        val contentResolver = context.contentResolver
                        val uri =
                            contentResolver.insert(extVolumeUri, myContentValues) ?: return@let
                        showLog(logTag_Doc, "file uri $uri")

                        try {
                            val outputStream =
                                contentResolver.openOutputStream(Objects.requireNonNull(uri))
                            try {
                                document.writeTo(outputStream)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            myContentValues.clear()
                            myContentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                            contentResolver.update(uri, myContentValues, null, null)
                        }
                        val fileName = uriToFileName(context, uri)
                        completeTask(fileName ?: "")
                    }
                }
                .setNegativeButton("CANCEL") { dialog: DialogInterface, id: Int -> dialog.cancel() }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(context, R.color.black))
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(context, R.color.black))
        }*/

        private fun uriToFileName(context: Context, uri: Uri): String? {
            var filePath: String? = null
            if (DocumentsContract.isDocumentUri(context, uri)) {
                val wholeID = DocumentsContract.getDocumentId(uri)
                val numSegments = wholeID.split(":").size
                if (numSegments == 2 && "external_storage".equals(
                        wholeID.substring(
                            0,
                            numSegments - 1
                        ), ignoreCase = true
                    )
                ) {
                    showLog(logTag_Doc, "external_storage")
                    val externalStorageVolumeName = wholeID.substring(numSegments - 1)
                    val externalStorageDir =
                        File(Environment.getExternalStorageDirectory(), externalStorageVolumeName)
                    val file = File(externalStorageDir, wholeID.substring(numSegments))
                    filePath = file.absolutePath
                } else if ("primary".equals(
                        wholeID.substring(0, numSegments - 1),
                        ignoreCase = true
                    )
                ) {
                    showLog(logTag_Doc, "primary")
                    val file =
                        File(context.getExternalFilesDir(null), wholeID.substring(numSegments))
                    filePath = file.absolutePath
                }
                val projection = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = context.contentResolver.query(uri, projection, null, null, null)
                cursor?.use { cursor ->
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    cursor.moveToFirst()
                    filePath = cursor.getString(columnIndex)
                }
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                filePath = uri.path.toString()
            }

            return filePath
        }

//        @OptIn(DelicateCoroutinesApi::class)
//        fun updateWidgetNotifyBroadCast(context: Context) {
//            GlobalScope.launch(Dispatchers.IO) {
//                showLog(
//                    logTag_Doc,
//                    "eventTable ${StatusSharedPreferences.getStatusSharedPreferences(context).tableEvent}"
//                )
//                val workRequest = OneTimeWorkRequestBuilder<WorkManagerWidget>().build()
//                WorkManager.getInstance(context).enqueue(workRequest)
////                val intent = Intent("com.bugbd.docfilescanner.WIDGET_UPDATE")
////                context.sendBroadcast(intent)
//            }
//
//        }

//        fun shouldShow(activity: Activity, context: Context, layoutInflater: LayoutInflater) {
//            when {
//                ActivityCompat.shouldShowRequestPermissionRationale(
//                    activity,
//                    Manifest.permission.CAMERA
//                )
//                        || ActivityCompat.shouldShowRequestPermissionRationale(
//                    activity,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                )
//                        || ActivityCompat.shouldShowRequestPermissionRationale(
//                    activity,
//                    Manifest.permission.READ_EXTERNAL_STORAGE
//                )
//                        || ActivityCompat.shouldShowRequestPermissionRationale(
//                    activity,
//                    Manifest.permission.READ_MEDIA_IMAGES
//                )
//                        || ActivityCompat.shouldShowRequestPermissionRationale(
//                    activity,
//                    Manifest.permission.POST_NOTIFICATIONS
//                )
//                -> {
//                    showLog(
//                        logTag_Doc,
//                        "showRequestPermissionDialog"
//                    )
//                    showRequestPermissionDialog(context, layoutInflater)
//                }
//
//                else -> {
//                    //showToast(context,"Need permission for camera")
//                    showLog(
//                        logTag_Doc,
//                        "no match permission"
//                    )
//                }
//
//            }
//        }


        fun checkShouldPermissionNeed(permissions: Array<String>, activity: Activity): Boolean {
            for (item in permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        item
                    )
                ) {
                    return true
                }
            }
            return false
        }

        fun getLanguageCode(item: LanguageItem): String {
            return if (item.title.contains("English")) {
                "en"
            } else if (item.title.contains("Bangla")) {
                "bn"
            } else if (item.title.contains("Arabic")) {
                "ar"
            } else {
                "hi"
            }
        }

        @SuppressLint("ServiceCast")
        fun copyTextToClipboard(context: Context, text: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        fun saveImageToStorage(context: Context, bitmap: Bitmap?): Uri? {
            var imageUri: Uri? = null
            val file: File? =
                if (context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) != null) {
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                } else {
                    context.filesDir
                }
            val derectory = File(file, "com.bugbd.wifiscane")
            if (!derectory.exists()) {
                derectory.mkdir()
            }
            derectory.let {
                val filename = System.currentTimeMillis().toString()
                val outPutFile =
                    File(derectory, filename + "_" + System.currentTimeMillis() + ".jpg")
                try {
                    val fileOutputStream = FileOutputStream(outPutFile)
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                    fileOutputStream.flush()
                    fileOutputStream.close()
                    imageUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        outPutFile
                    )
                } catch (e: FileNotFoundException) {
                    imageUri = null
                    e.printStackTrace()
                }
            }
            return imageUri
        }

        fun shareImage(context: Context, imageUri: Uri, text: String) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, imageUri) // Add image URI
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION // Grant read permission
            }
            context.startActivity(Intent.createChooser(shareIntent, text))
        }


        @Suppress("DEPRECATION")
        fun connectToWifiLegacy(context: Context, ssid: String, password: String) {
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiConfig = WifiConfiguration().apply {
                SSID = "\"$ssid\""
                preSharedKey = "\"$password\""
            }
            val networkId = wifiManager.addNetwork(wifiConfig)
            wifiManager.disconnect()
            wifiManager.enableNetwork(networkId, true)
            wifiManager.reconnect()
        }

//        fun intiProgressDialog(context: Context, layoutInflater: LayoutInflater): Dialog {
//            val progressDialog = Dialog(context)
//            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//            val binding = CustomProgressDialogBinding.inflate(layoutInflater)
//            progressDialog.setContentView(binding.root)
//            progressDialog.window?.setLayout(120, 120)
//            progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//            progressDialog.setCancelable(false)
//
//            return progressDialog
//        }

        /*@SuppressLint("MissingPermission")
        fun connectToWifi(
            context: Context,
            ssid: String,
            password: String,
            onStatus: (connect: Boolean) -> Unit
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
                        .setSsid(ssid)
                        .setWpa2Passphrase(password)
                        .build()

                    val networkRequest = NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .setNetworkSpecifier(wifiNetworkSpecifier)
                        .build()

                    val connectivityManager =
                        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                    val callback = object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            super.onAvailable(network)
                            Handler(Looper.getMainLooper()).post {
                                connectivityManager.bindProcessToNetwork(network)
                                onStatus(true)
                            }
                        }

                        override fun onUnavailable() {
                            super.onUnavailable()
                            Handler(Looper.getMainLooper()).post {
                                onStatus(false)
                            }
                        }

                        override fun onLost(network: Network) {
                            super.onLost(network)
                            Handler(Looper.getMainLooper()).post {
                                onStatus(false)
                            }
                        }
                    }

                    connectivityManager.requestNetwork(networkRequest, callback)

                } catch (e: Exception) {
                    e.printStackTrace()
                    onStatus(false)
                }

            } else {
                try {
                    val wifiManager =
                        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

                    val wifiConfig = WifiConfiguration().apply {
                        SSID = "\"$ssid\""
                        preSharedKey = "\"$password\""
                    }

                    val netId = wifiManager.addNetwork(wifiConfig)
                    if (netId != -1) {
                        wifiManager.disconnect()
                        wifiManager.enableNetwork(netId, true)
                        wifiManager.reconnect()
                        onStatus(true)
                    } else {
                        onStatus(false)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    onStatus(false)
                }
            }
        }*/

        fun renderFirstPageFromPdfUri(
            context: Context,
            pdfUri: Uri,
            onResult: (bitmap: Bitmap?, totalPages: Int) -> Unit
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                var bitmap: Bitmap? = null
                var totalPages = 0

                val contentResolver = context.contentResolver
                val fileDescriptor = contentResolver.openFileDescriptor(pdfUri, "r")
                if (fileDescriptor != null) {
                    val renderer = PdfRenderer(fileDescriptor)
                    totalPages = renderer.pageCount

                    if (totalPages > 0) {
                        val page = renderer.openPage(0)
                        bitmap = createBitmap(page.width, page.height)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                    }

                    renderer.close()
                    fileDescriptor.close()
                }

                // Switch to Main thread to return the result
                withContext(Dispatchers.Main) {
                    onResult(bitmap, totalPages)
                }
            }
        }
//        fun ImageView.loadPdfFirstPageFromUri(uri: Uri, context: Context) {
//            val placeholderDrawable =
//            renderFirstPageFromPdfUri(context, uri)?.let {
//                this.load(it) {
//                    transformations(RoundedCornersTransformation(16f))
//                    placeholder(ContextCompat.getDrawable(context, R.drawable.baseline_file_copy_24))
//                    error(ContextCompat.getDrawable(context, R.drawable.baseline_file_copy_24))
//                }
//            }
//        }

        fun formatFileSize(bytes: Long): String {
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            return when {
                mb >= 1 -> String.format("%.2f MB", mb)
                kb >= 1 -> String.format("%.2f KB", kb)
                else -> "$bytes bytes"
            }
        }
        fun getPdfFileSizeFromUri(context: Context, uri: Uri): Long {
            return context.contentResolver.openAssetFileDescriptor(uri, "r")?.use {
                it.length
            } ?: 0L
        }

        fun getAllPdfFilesWithDetails(context: Context, items: (items:List<PdfFileItem>) -> Unit) {
            try {
                "getAllPdfFilesWithDetails() get scan file  ".logD()
                    val pdfList = mutableListOf<PdfFileItem>()

                    val collection = MediaStore.Files.getContentUri("Internal")
                    val projection = arrayOf(
                        MediaStore.Files.FileColumns._ID,
                        MediaStore.Files.FileColumns.DISPLAY_NAME,
                        MediaStore.Files.FileColumns.SIZE,
                        MediaStore.Files.FileColumns.MIME_TYPE,
                        MediaStore.Files.FileColumns.DATE_ADDED // or DATE_MODIFIED
                    )

                    val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
                    val selectionArgs = arrayOf("*")

                    val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

                    val cursor = context.contentResolver.query(
                        collection,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder
                    )

                    cursor?.use {
                        val idCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                        val nameCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                        val sizeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                        val dateAddedCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)

                        while (it.moveToNext()) {
                            val id = it.getLong(idCol)
                            val name = it.getString(nameCol) ?: "Unknown"
                            val size = it.getLong(sizeCol)
                            val dateAddedSec = it.getLong(dateAddedCol)
                            val createdTimeMillis = dateAddedSec * 1000

                            val uri = ContentUris.withAppendedId(collection, id)
                            val pageCount = getPdfPageCountFromUri(context, uri)

                            pdfList.add(PdfFileItem(uri, name, size, pageCount, createdTimeMillis))
                        }
                    }

                    if (pdfList.isNotEmpty()){
                        "size: ${pdfList.size}".logD()
                        items(pdfList)
                    }else{
                        "empty list : ${pdfList.size}".logD()
                        items(listOf())
                    }
            }catch (e:Exception){
                "exception: ${e.printStackTrace()}".logD()
                items(listOf())
            }
        }


        fun getAllPdfBrowse(context: Context,items: (items:List<PdfFileItem>) -> Unit) {
            val pdfList = mutableListOf<PdfFileItem>()

            val collections = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                listOf(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                )
            } else {
                listOf(
                    MediaStore.Files.getContentUri("external") // fallback
                )            }

            for (collection in collections) {
                val projection = arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.MIME_TYPE
                )

                val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
                val selectionArgs = arrayOf("application/pdf")
                val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

                val cursor = context.contentResolver.query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )

                cursor?.use {
                    val idCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    val nameCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val sizeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                    val dateCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)

                    while (it.moveToNext()) {
                        val id = it.getLong(idCol)
                        val name = it.getString(nameCol)
                        val size = it.getLong(sizeCol)
                        val dateAdded = it.getLong(dateCol) * 1000
                        val uri = ContentUris.withAppendedId(collection, id)

                        val pageCount = getPdfPageCountFromUri(context, uri) // Your existing logic

                        pdfList.add(PdfFileItem(uri, name, size, pageCount, dateAdded))
                    }
                }
            }
            items(pdfList)
        }

        fun getAllPdfFilesFromStorage(context: Context,items: (items:List<PdfFileItem>) -> Unit) {
            val pdfList = mutableListOf<PdfFileItem>()
            val publicDirs = listOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            )

            for (dir in publicDirs) {
                if (dir.exists()) {
                    scanDirectoryForPdfs(context, dir, pdfList)
                }
            }
            items(pdfList)
        }

        fun scanDirectoryForPdfs(context: Context, dir: File, pdfList: MutableList<PdfFileItem>) {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    scanDirectoryForPdfs(context, file, pdfList)
                } else if (file.extension.equals("pdf", ignoreCase = true)) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                    val name = file.name
                    val size = file.length()
                    val createdTime = file.lastModified()
                    val pageCount = getPdfPageCountFromFile(file)

                    pdfList.add(PdfFileItem(uri, name, size, pageCount, createdTime))
                }
            }
        }


        fun fetchAllPdfFiles(context: Context, onResult: (List<PdfFileItem>) -> Unit) {
                val pdfList = mutableListOf<PdfFileItem>()
                val directories = listOf(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    Environment.getExternalStorageDirectory(),
                    Environment.getDataDirectory(),
                    Environment.getDownloadCacheDirectory()
                )

                for (dir in directories) {
                    if (dir != null && dir.exists()) {
                        scanForPdfFiles(context, dir, pdfList)
                    }
                }
                onResult(pdfList)
        }

        private fun scanForPdfFiles(context: Context, dir: File, list: MutableList<PdfFileItem>) {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    scanForPdfFiles(context, file, list)
                } else if (file.extension.equals("pdf", ignoreCase = false)) {
                    val uri = Uri.fromFile(file)
                    val name = file.name
                    val size = file.length()
                    val createdTime = file.lastModified()
                    val pageCount = getPdfPageCountFromFile(file) // We'll define this next

                    list.add(PdfFileItem(uri, name, size, pageCount, createdTime))
                }
            }
        }

        fun getAllPdfUris(context: Context): List<Uri> {
            val pdfList = mutableListOf<Uri>()
            val collection = MediaStore.Files.getContentUri("external")

            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.MIME_TYPE
            )

            val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
            val selectionArgs = arrayOf("application/pdf")

            val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(collection, id)
                    pdfList.add(contentUri)
                }
            }

            return pdfList
        }



        fun getPdfPageCountFromFile(file: File): Int {
            return try {
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                val count = renderer.pageCount
                renderer.close()
                pfd.close()
                count
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }
        }



        @SuppressLint("Range")
        fun getAllPdfFiles(context: Context, items: (List<PdfFileItem>) -> Unit) {
            try {
                "getAllPdfFilesWithDetails() - Scanning files".logD()
                val pdfList = mutableListOf<PdfFileItem>()

                val uriExternal: Uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
                val projection = arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.MIME_TYPE
                )

                val selection = "${MediaStore.Files.FileColumns.MIME_TYPE}=?"
                val selectionArgs = arrayOf("application/pdf")
                val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

                val cursor = context.contentResolver.query(
                    uriExternal,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )

                cursor?.use {
                    while (it.moveToNext()) {
                        val id = it.getLong(it.getColumnIndex(MediaStore.Files.FileColumns._ID))
                        val name = it.getString(it.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)) ?: "Unnamed"
                        val size = it.getLong(it.getColumnIndex(MediaStore.Files.FileColumns.SIZE))
                        val dateAdded = it.getLong(it.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)) * 1000
                        val uri = ContentUris.withAppendedId(uriExternal, id)

                        val pageCount = getPdfPageCountFromUri(context, uri) // Your existing logic

                        pdfList.add(PdfFileItem(uri, name, size, pageCount, dateAdded))
                    }
                }

                if (pdfList.isNotEmpty()) {
                    "PDF files found: ${pdfList.size}".logD()
                    items(pdfList)
                } else {
                    "No PDF files found".logD()
                    items(emptyList())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "Exception while fetching PDFs: ${e.localizedMessage}".logD()
                items(emptyList())
            }
        }

        @SuppressLint("Range")
        fun getAllDocumentFiles(context: Context, onResult: (List<PdfFileItem>) -> Unit) {
            val documents = mutableListOf<PdfFileItem>()

            val contentUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED
            )

            val mimeTypes = arrayOf(
                "pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",       // .xlsx
                "text/plain",
                "application/rtf",
                "application/vnd.oasis.opendocument.text",                                 // .odt
                "application/vnd.oasis.opendocument.spreadsheet"                           // .ods
            )

            val selection = mimeTypes.joinToString(" OR ") { "${MediaStore.Files.FileColumns.MIME_TYPE} = ?" }
            val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

            val cursor = context.contentResolver.query(
                contentUri,
                projection,
                selection,
                mimeTypes,
                sortOrder
            )

            cursor?.use {
                val idCol = it.getColumnIndex(MediaStore.Files.FileColumns._ID)
                val nameCol = it.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeCol = it.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                val dateCol = it.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)
                val mimeCol = it.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val name = it.getString(nameCol)
                    val size = it.getLong(sizeCol)
                    val dateAddedMillis = it.getLong(dateCol) * 1000
                    val mime = it.getString(mimeCol) ?: ""

                    val uri = ContentUris.withAppendedId(contentUri, id)

                    val pageCount = if (mime == "application/pdf") {
                        getPdfPageCountFromUri(context, uri)
                    } else 0

                    documents.add(PdfFileItem(uri, name, size, pageCount, dateAddedMillis))
                }
            }

            onResult(documents)
        }



        @SuppressLint("Range")
        fun getAllDocumentsFromDocumentsFolder(context: Context, items: (List<PdfFileItem>) -> Unit) {
            try {
                "getAllDocumentsFromDocumentsFolder() - Start".logD()
                val documentList = mutableListOf<PdfFileItem>()

                val uriExternal = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)

                val projection = arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.RELATIVE_PATH
                )

                // Filter for common document types
                val mimeTypes = arrayOf(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "text/plain"
                )

                // Create selection query
                val selection = "${MediaStore.Files.FileColumns.RELATIVE_PATH} LIKE ?"
                val selectionArgs = arrayOf("%Documents/%")

                val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

                val cursor = context.contentResolver.query(
                    uriExternal,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )

                cursor?.use {
                    while (it.moveToNext()) {
                        val id = it.getLong(it.getColumnIndex(MediaStore.Files.FileColumns._ID))
                        val name = it.getString(it.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)) ?: "Unnamed"
                        val size = it.getLong(it.getColumnIndex(MediaStore.Files.FileColumns.SIZE))
                        val mimeType = it.getString(it.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)) ?: "unknown"
                        val dateAdded = it.getLong(it.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)) * 1000
                        val uri = ContentUris.withAppendedId(uriExternal, id)

                        val pageCount = if (mimeType == "application/pdf") {
                            getPdfPageCountFromUri(context, uri)
                        } else {
                            0
                        }

                        documentList.add(PdfFileItem(uri, name, size, pageCount, dateAdded))
                    }
                }

                items(documentList)
            } catch (e: Exception) {
                e.printStackTrace()
                "Exception while loading documents: ${e.localizedMessage}".logD()
                items(emptyList())
            }
        }


        private fun getPdfPageCountFromUri(context: Context, uri: Uri): Int {
            return try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        renderer.pageCount
                    }
                } ?: 0
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }
        }

//

    }

}