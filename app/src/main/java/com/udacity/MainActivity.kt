package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Fade
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

const val EXTRA_STATUS = "status"
const val EXTRA_FILE_NAME = "file_name"

class MainActivity : AppCompatActivity() {

    private var radioChoice: Int = -1
    private lateinit var status: String
    private var downloadID: Long = 0

    private lateinit var downloadManager: DownloadManager

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
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(id!!))
            if (cursor.moveToNext()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                cursor.close()
                when (status) {
                    DownloadManager.STATUS_FAILED -> this@MainActivity.status = "Failed"
                    DownloadManager.STATUS_SUCCESSFUL -> this@MainActivity.status = "Successful"
                    else -> this@MainActivity.status = "Undefined"
                }
            }

            notificationManager.sendNotification(context!!)
            /**
             * For the purpose of the project set custom button state to Completed
             * three seconds after notification has been sent. This is the time necessary
             * for full animation of the custom button.
             */
            setCustomButtonCompletedAfterThreeSeconds()
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
        val bigPicture = decodeBigPic(radioChoice)
        val largeIcon = decodeLargeIcon(radioChoice)
        val bigPicStyle = NotificationCompat.BigPictureStyle()
            .bigPicture(bigPicture)
            .bigLargeIcon(null)

        val contentIntent = Intent(appContext, DetailActivity::class.java).apply {
            putExtra(EXTRA_STATUS, status)
            putExtra(
                EXTRA_FILE_NAME,
                when (radioChoice) {
                    0 -> getString(R.string.glide_file_name)
                    1 -> getString(R.string.app_file_name)
                    else -> getString(R.string.retrofit_file_name)
                }
            )
        }
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
            .setLargeIcon(largeIcon)
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

            downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
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
                        radioChoice = 0
                    }
                R.id.radio_app ->
                    if (checked) {
                        URL = getString(R.string.app_url)
                        radioChoice = 1
                    }
                R.id.radio_retrofit ->
                    if (checked) {
                        URL = getString(R.string.retrofit_url)
                        radioChoice = 2
                    }
                else -> radioChoice = -1
            }
        }
    }

    private fun decodeBigPic(choice: Int): Bitmap {
        return when (choice) {
            0 -> BitmapFactory.decodeResource(resources, R.drawable.glide_big_pic)
            1 -> BitmapFactory.decodeResource(resources, R.drawable.app_big_pic)
            else -> BitmapFactory.decodeResource(resources, R.drawable.retrofit_big_pic)
        }
    }

    private fun decodeLargeIcon(choice: Int): Bitmap {
        return when (choice) {
            0 -> BitmapFactory.decodeResource(resources, R.drawable.glide_large_icon)
            1 -> BitmapFactory.decodeResource(resources, R.drawable.app_large_icon)
            else -> BitmapFactory.decodeResource(resources, R.drawable.retrofit_large_icon)
        }
    }

    private fun setCustomButtonCompletedAfterThreeSeconds() {
        val handler = Handler(Looper.getMainLooper()).postDelayed(Runnable {
            custom_button.changeButtonState(ButtonState.Completed)
        }, THREE_SECONDS)
    }

    companion object {
        private var URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
        private const val NOTIFICATION_ID = 0
        private const val THREE_SECONDS = 3_000L
    }

}
