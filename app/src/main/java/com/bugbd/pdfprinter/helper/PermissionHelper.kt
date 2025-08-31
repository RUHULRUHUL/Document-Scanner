package com.bugbd.pdfprinter.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionHelper(
    private val context: Context,
    private val activity: ComponentActivity,
    private val onPermissionGranted: () -> Unit
) {
    fun initLauncher(register: (ActivityResultLauncher<Array<String>>) -> Unit) {
        register.invoke(createLauncher())
    }

    private fun createLauncher(): ActivityResultLauncher<Array<String>> {
        return activity.activityResultRegistry.register(
            "permission_launcher_${System.currentTimeMillis()}",
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val allGranted = result.all { it.value }
            if (allGranted) {
                onPermissionGranted()
            } else {
                Toast.makeText(context, "This app needs these permissions to work properly.", Toast.LENGTH_SHORT).show()
                openAppSettings(context)
            }
        }
    }
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun checkAndRequestPermissions(permissions: Array<String>,launcher: ActivityResultLauncher<Array<String>>) {
        if (arePermissionsGranted(activity, permissions)) {
            onPermissionGranted()
        } else {
            if (shouldShowRationale(activity, permissions)) {
                showRationaleDialog {
                    launcher.launch(permissions)
                }
            } else {
                launcher.launch(permissions)
            }
        }
    }

    private fun showRationaleDialog(onProceed: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("Permission Required")
            .setMessage("This app needs these permissions to work properly.")
            .setPositiveButton("Allow") { _, _ -> onProceed() }
            .setNegativeButton("Cancel") { _, _ ->
                Toast.makeText(context, "This app needs these permissions to work properly.", Toast.LENGTH_SHORT).show()

            }
            .show()
    }

    companion object {
        fun arePermissionsGranted(context: Context, permissions: Array<String>): Boolean {
            return permissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        }

        fun shouldShowRationale(activity: Activity, permissions: Array<String>): Boolean {
            return permissions.any {
                ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
            }
        }
    }
}
