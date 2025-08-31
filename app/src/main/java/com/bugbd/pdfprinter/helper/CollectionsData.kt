package com.bugbd.pdfprinter.helper

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bugbd.qrcode.model.LanguageItem
import java.io.File
import java.io.Serializable

data class Item(
    val name: String = "",
    val icon: Int = 0,
    val type: Constants.QrCodeType = Constants.QrCodeType.COMMON
)

data class PdfFileItem(
    val uri: Uri,
    val name: String,
    val sizeInBytes: Long,
    val pageCount: Int,
    val createdTimeMillis: Long

)

data class PdfFile(
    val file: File,
    val name: String
)


@Entity
data class WifiNetwork(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var SSID: String = "",          // Wi-Fi network name
    val BSSID: String = "",         // Basic Service Set Identifier (MAC address)
    val capabilities: String = "",  // Describes the authentication, key management, and encryption schemes supported by the access point
    val frequency: Int = 0,        // Frequency in MHz (e.g., 2412, 5180)
    val level: Int = 0,            // Signal strength (RSSI) in dBm (e.g., -55)
    val timestamp: Long = 0,
    var password: String = ""// Timestamp when the scan result was last seen
) : Serializable


val drawerList = listOf(
    LanguageItem(title = "English", isSelect = false),
    LanguageItem(title = "Bangla", isSelect = false),
    LanguageItem(title = "Arabic", isSelect = false),
    LanguageItem(title = "Hindi", isSelect = false)
)


data class WiFiType(
    val name: String
)

val wifiTypes = arrayListOf(
    "WPA",
    "WPA2",
    "WPA3",
    "WEP",
    "nopass"
)

data class Location(
    val locationName: String = "",
    val lat: String = "",
    val lon: String = "",
    val url: String = ""
)

data class Response(
    val status: Boolean = false,
    val message: String = "",
)

fun storagePermission(): MutableList<String> {
    val permissionList = mutableListOf<String>()
    permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    return  permissionList
}

fun getRequiredPermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.POST_NOTIFICATIONS,
        )
    } else {
        arrayOf(
            Manifest.permission.CAMERA,
        )
    }
}


data class MediaFile(
    val uri: Uri,
    val name: String,
    val type: MediaType
)

enum class MediaType {
    IMAGE,
    VIDEO,
    AUDIO,
    PDF,
}

fun getAllMediaFiles(context:Context): List<MediaFile> {
    val skipQuery = if(Build.VERSION.SDK_INT <= 32) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    } else false

    if(skipQuery) {
        return emptyList()
    }

    val mediaFiles = mutableListOf<MediaFile>()

    val queryUri = if(Build.VERSION.SDK_INT >= 29) {
        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else MediaStore.Files.getContentUri("external")

    val projection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.MIME_TYPE,
    )

    val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
    val selectionArgs = arrayOf("application/pdf")

    val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

    context.contentResolver.query(
        queryUri,
        projection,
        selection,
        selectionArgs,
        sortOrder
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(
            MediaStore.Files.FileColumns._ID
        )
        val nameColumn = cursor.getColumnIndexOrThrow(
            MediaStore.Files.FileColumns.DISPLAY_NAME
        )
        val mimeTypeColumn = cursor.getColumnIndexOrThrow(
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        while(cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val name = cursor.getString(nameColumn)
            val mimeType = cursor.getString(mimeTypeColumn)

            if(name != null && mimeType != null) {
                val contentUri = ContentUris.withAppendedId(
                    queryUri,
                    id
                )
                val mediaType = when {
                    mimeType.startsWith("audio/") -> MediaType.AUDIO
                    mimeType.startsWith("video/") -> MediaType.VIDEO
                    mimeType.startsWith("pdf/") -> MediaType.PDF
                    else -> MediaType.IMAGE
                }

                mediaFiles.add(
                    MediaFile(
                        uri = contentUri,
                        name = name,
                        type = mediaType
                    )
                )
            }
        }
    }

    return mediaFiles.toList()
}

val files = getPdfs(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS))

fun getPdfs(file: File): MutableList<File> {
    val pdfFiles = mutableListOf<File>()
    file.listFiles()?.forEach {
        if (it.isDirectory && !it.isHidden) {
            "directory: ${it.name}".logD()
            pdfFiles.addAll(getPdfs(it))
        }else{
            "file: ${it.name}".logD()
            if (it.name.endsWith(".pdf",false)){
                pdfFiles.add(it)
            }
        }
    }
    return pdfFiles
}

fun requestAllFilesAccessPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (!Environment.isExternalStorageManager()) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:" + context.packageName)
                context.startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_SETTINGS)
                context.startActivity(intent)
            }
        }
    }
}
