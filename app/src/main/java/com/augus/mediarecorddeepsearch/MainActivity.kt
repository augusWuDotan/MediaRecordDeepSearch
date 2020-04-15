package com.augus.mediarecorddeepsearch

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.augus.mediarecorddeepsearch.lib.MediaRecordManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

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
    private var permissionToRecordAccepted = false
    private var filePath: String = ""
    private var fileName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // .append(getString(R.string.app_name)) .append("/")
        filePath = StringBuffer()
            .append(this.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!.path)
            .toString()
        fileName = "RecordName_${(Math.random() * 100).toInt()}_dd.m4a"
        Log.d(this.packageName, "filePath:${filePath} fileName:${fileName} ")
        var file = File(filePath,fileName)
        if(!file.exists()){
            Log.d(this.packageName, "file exists :${file.exists()}")
            Log.d(this.packageName, "file parentFile isDirectory :${file.parentFile.isDirectory}")
            Log.d(this.packageName, "file createNewFile :${file.createNewFile()}")
        }

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

    fun test() {
        MediaRecordManager.instance
            .setRecordFileName(fileName, filePath)
            .createMediaRecord()
            .prepareRecord()

        GlobalScope.launch {
            MediaRecordManager.instance.startRecord()
            delay(2000L)
            runOnUiThread {
                MediaRecordManager.instance.stopRecord()
            }
        }
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
