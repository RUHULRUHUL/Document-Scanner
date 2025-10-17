package com.bugbd.pdfprinter.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bugbd.pdfprinter.MainActivity
import com.bugbd.pdfprinter.R
import com.bugbd.pdfprinter.helper.Utils.Companion.channelID
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class NotificationSystem : FirebaseMessagingService() {
    private var pendingIntent: PendingIntent? = null
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("message", remoteMessage.notification?.body)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT )
        showNotification(remoteMessage)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun showNotification(remoteMessage: RemoteMessage) {
        val nManager = NotificationManagerCompat.from(this)
        try {
            if (remoteMessage.notification?.imageUrl != null) {
                ImageDownload(remoteMessage).execute(remoteMessage.notification?.imageUrl.toString())
            } else {
                val builder: NotificationCompat.Builder =
                    NotificationCompat.Builder(this, channelID)
                        .setSmallIcon(R.drawable.ic_pdf_img)
                        .setContentTitle(remoteMessage.notification?.title)
                        .setContentText(remoteMessage.notification?.body)
                        .setStyle(
                            NotificationCompat.BigTextStyle().bigText(
                                remoteMessage.notification!!.body
                            )
                        )
                        .setContentIntent(pendingIntent)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setAutoCancel(false)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        nManager.notify(0, builder.build())
                    }else{
                        nManager.notify(0, builder.build())
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showBannerNotification(bitmap: Bitmap, remoteMessage: RemoteMessage?) {
        if (remoteMessage != null) {
            val builder: NotificationCompat.Builder =
                NotificationCompat.Builder(this, channelID)
                    .setSmallIcon(R.drawable.ic_pdf_img)
                    .setContentTitle(remoteMessage.notification?.title)
                    .setContentText(remoteMessage.notification?.body)
                    .setStyle(NotificationCompat.BigPictureStyle().bigLargeIcon(bitmap))
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setAutoCancel(false)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

                val notificationManagerCompat = NotificationManagerCompat.from(this)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    notificationManagerCompat.notify(0, builder.build())
                }else{
                    notificationManagerCompat.notify(0, builder.build())
                }
        }
    }


    @SuppressLint("StaticFieldLeak")
    inner class ImageDownload(private val remoteMessage: RemoteMessage) :
        AsyncTask<String?, Void?, Bitmap?>() {
        @Deprecated("Deprecated in Java")
        override fun onPostExecute(bitmap: Bitmap?) {
            if (bitmap != null) {
                showBannerNotification(bitmap, remoteMessage)
            }
        }

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: String?): Bitmap? {
            val inputStream: InputStream
            try {
                val url = URL(params[0])
                try {
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connect()
                    inputStream = connection.inputStream
                    return BitmapFactory.decodeStream(inputStream)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
            return null
        }
    }


}