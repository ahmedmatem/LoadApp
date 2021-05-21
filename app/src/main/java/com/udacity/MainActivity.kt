package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(CHANNEL_ID, getString(R.string.channel_name))

        custom_button.setOnClickListener {
            // clear previous notification from status bar
            notificationManager.cancelAll()
            download()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
//            Toast.makeText(context, "id: $id", Toast.LENGTH_LONG).show()
            notificationManager.sendNotification(context!!)
        }
    }

    private fun createNotificationChannel(channelId: String, name: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT)
                    .apply {
                        description = getString(R.string.notification_description)
                        enableLights(true)
                        lightColor = getColor(R.color.colorAccent)
                        enableVibration(true)
                        setShowBadge(false)
                    }
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun NotificationManager.sendNotification(appContext: Context) {
        val glideBigPic = BitmapFactory.decodeResource(
            resources,
            R.drawable.glide_bic_pic
        )
        val glideLargeIcon = BitmapFactory.decodeResource(
            resources,
            R.drawable.glide_large_icon
        )
        val bigPicStyle = NotificationCompat.BigPictureStyle()
            .bigPicture(glideBigPic)
            .bigLargeIcon(null)

        val contentIntent = Intent(appContext, DetailActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            appContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.cloud_download)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_description))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setStyle(bigPicStyle)
            .setLargeIcon(glideLargeIcon)
            .addAction(
                R.drawable.ic_assistant_black_24dp,
                getString(R.string.notification_button),
                contentPendingIntent
            )

        notify(NOTIFICATION_ID, builder.build())
    }

    private fun download() {
        if (radio_group.checkedRadioButtonId == -1) {
            Toast.makeText(this, R.string.radio_group_unchecked_toast_text, Toast.LENGTH_LONG)
                .show()
        } else {
            custom_button.changeButtonState(ButtonState.Loading)
            val request =
                DownloadManager.Request(Uri.parse(URL))
                    .setTitle(getString(R.string.app_name))
                    .setDescription(getString(R.string.app_description))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID =
                downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked
            when (view.id) {
                R.id.radio_glide ->
                    if (checked) {
                        URL = getString(R.string.glide_url)
                    }
                R.id.radio_app ->
                    if (checked) {
                        URL =
                            getString(R.string.app_url)
                    }
                R.id.radio_retrofit ->
                    if (checked) {
                        URL = getString(R.string.retrofit_url)
                    }
            }
        }
    }

    companion object {
        private var URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
        private const val NOTIFICATION_ID = 0
    }

}
