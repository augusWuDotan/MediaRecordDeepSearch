package com.augus.mediarecorddeepsearch

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.augus.mediarecorddeepsearch.lib.MediaRecordConstants
import com.augus.mediarecorddeepsearch.lib.MediaRecordManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {


    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }

    private var permissions: Array<String> =
        arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    private var filePath: String = ""
    private var fileName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        } else {
            test()
        }
    }

    fun onCaptureClick(view: View?) {
        Log.d(this.packageName, "getStatus: ${MediaRecordManager.instance.getStatus()}")
        if (MediaRecordManager.instance.getStatus() == MediaRecordConstants.MEDIA_RECORD_STATUS_PREPAR) {
            MediaRecordManager.instance.startRecord()
        } else if (MediaRecordManager.instance.getStatus() == MediaRecordConstants.MEDIA_RECORD_STATUS_START) {
            MediaRecordManager.instance.stopRecord()
        }else{

        }
    }

    fun test() {
        MediaRecordManager.instance
            .setRecordFileName(this)
            .createMediaRecord()
            .prepareRecord()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(this.packageName, "grantResults: ${grantResults.size}")
        var permissionToRecordAccepted = true
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults.forEach {
                if (it != PackageManager.PERMISSION_GRANTED) permissionToRecordAccepted = false
            }
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
        else {
            test()
        }
    }
}
