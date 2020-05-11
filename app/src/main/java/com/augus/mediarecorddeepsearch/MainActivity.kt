package com.augus.mediarecorddeepsearch

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.augus.mediarecorddeepsearch.lib.MediaRecordConstants
import com.augus.mediarecorddeepsearch.lib.MediaRecordManager
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
    private var filePath: String = ""
    private var fileName: String = ""

    //
    private var mMediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        executor.scheduleAtFixedRate(mRunnable, 0, 100, TimeUnit.MILLISECONDS)
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
        mMediaPlayer?.reset()
        Log.d(this.packageName, "getStatus: ${MediaRecordManager.instance.getRecordStatus()}")
        if (MediaRecordManager.instance.getRecordStatus() == MediaRecordConstants.MEDIA_RECORD_STATUS_PREPAR) {
            MediaRecordManager.instance.startRecord()
        } else if (MediaRecordManager.instance.getRecordStatus() == MediaRecordConstants.MEDIA_RECORD_STATUS_START) {
            MediaRecordManager.instance.stopRecord()
        } else {
            MediaRecordManager.instance.recycleRecorderFile()
        }
    }

    fun test() {
        mMediaPlayer = MediaPlayer()
        mMediaPlayer?.setOnPreparedListener {
//            it.start()
        }
        mMediaPlayer?.setOnErrorListener { mp, what, extra ->
            Log.d("tOnErrorListener", "what:$what,extra:$extra")
            mp.reset()
            true
        }
        //非必要
        //.setAudioChannels(2)
        //.setAudioSource(MediaRecorder.AudioSource.MIC)
        //.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        //.setAudioEncoder(MediaRec
        // order.AudioEncoder.AAC)
        //.setAudioEncodingBitRate(96000)
        //.setAudioSamplingRate(44100)

        //第一次
        MediaRecordManager.instance
            .setRecorderListener(mMediaRecordListener)
            .setAudioChannels(2)
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            .setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            .setAudioEncodingBitRate(96000)
            .setAudioSamplingRate(44100)
            .createMediaRecord(this)
            .prepareRecord()

        //後續
//        MediaRecordManager.instance
//            .createMediaRecord(this)
//            .prepareRecord()
//            .startRecord()
    }

    private var mMediaRecordListener = object : MediaRecordManager.MediaRecordListener {

        override fun prepareSucess() {
            Log.d("mMediaRecordListener", "prepareSucess")
        }

        override fun recordStart() {
            Log.d("mMediaRecordListener", "recordStart")
        }

        override fun showDecibel(decibelPercentage: Float) {
            Log.d("mMediaRecordListener", "showDecibel decibelPercentage:$decibelPercentage")
        }

        override fun recordSuccess(file: File?) {
            Log.d("mMediaRecordListener", "recordSuccess file:${file?.length()}")
            if (file != null) {
                mMediaPlayer?.setDataSource(file.path)
                mMediaPlayer?.prepare()
            }
        }

        override fun recordStop() {
            Log.d("mMediaRecordListener", "recordStop")
        }

        override fun recordError(error: String) {
            Log.d("mMediaRecordListener", "recordError error:$error")
        }

        override fun recordRecycleSuccess(isSuccess: Boolean) {
            Log.d("mMediaRecordListener", "recordRecycleSuccess isSuccess:$isSuccess")
            MediaRecordManager.instance
                .createMediaRecord(this@MainActivity)
                .prepareRecord()
                .startRecord()
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
