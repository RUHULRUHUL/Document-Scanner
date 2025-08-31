package com.bugbd.pdfprinter.helper

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.core.content.ContextCompat

class PhotoPickerHelper(
    private val context: Context,
    private val activity: ComponentActivity,
    private val onPhotoPicked: (Uri?) -> Unit
) {

    // Register for Photo Picker (for Android 13+)
    private val pickMedia = activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        onPhotoPicked(uri)
    }

    // Register for Gallery Picker (for Android 12 and below)
    private val galleryLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            onPhotoPicked(uri)
        }
    }

    // Register for storage permission (for Android 12 and below)
    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchGalleryPicker()
        } else {
            showPermissionDeniedMessage() // User denied permission, show message
        }
    }

    // Public method to launch the photo picker
    fun pickPhoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33): Use Photo Picker
            launchPhotoPicker()
        } else {
            // Android 12 and below: Check permission and launch gallery picker
            checkPermissionAndPickImage()
        }
    }

    // Method to launch Photo Picker (Android 13+)
    private fun launchPhotoPicker() {
        pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
    }

    // Check storage permission and launch gallery (for Android 12 and below)
    private fun checkPermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED) {
            launchGalleryPicker()
        } else {
            // Show rationale if necessary
            if (activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showPermissionRationale()
            } else {
                // Request permission directly
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    // Show a dialog explaining why the permission is needed (Educational UI)
    private fun showPermissionRationale() {
        AlertDialog.Builder(context)
            .setTitle("Permission Needed")
            .setMessage("This app requires access to your gallery to pick photos. Please grant the permission to continue.")
            .setPositiveButton("OK") { _, _ ->
                // Request permission after the user sees the rationale
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    // Show a message if the user denies the permission
    private fun showPermissionDeniedMessage() {
        Toast.makeText(context, "Permission denied. Can't pick images from gallery.", Toast.LENGTH_SHORT).show()
    }

    // Method to launch gallery picker (for Android 12 and below)
    private fun launchGalleryPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }
}
