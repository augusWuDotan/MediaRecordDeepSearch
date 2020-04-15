package com.augus.mediarecorddeepsearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.augus.mediarecorddeepsearch.lib.MediaRecordManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MediaRecordManager.instance(this)
    }
}
