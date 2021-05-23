package com.udacity

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        // remove notification from status bar
        (getSystemService(NotificationManager::class.java) as NotificationManager)
        .run {
            cancelAll()
        }

        if (intent.hasExtra(EXTRA_STATUS)) {
            status.text = intent.getStringExtra(EXTRA_STATUS)!!
        }
        if (intent.hasExtra(EXTRA_FILE_NAME)) {
            fileName.text = intent.getStringExtra(EXTRA_FILE_NAME)!!
        }

        okButton.setOnClickListener {
            finish()
        }
    }

}
