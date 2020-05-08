package com.augus.mediarecorddeepsearch.lib

import android.content.Context
import android.media.MediaRecorder
import android.os.AsyncTask
import android.util.Log
import java.io.File
import java.io.IOException


class MediaRecordManager {
    companion object {
        val instance = MediaRecordHolder.initManager
    }

    object MediaRecordHolder {
        val initManager = MediaRecordManager()
    }

    interface MediaRecordListener {
        fun recordSuccess(file: File)
        fun recordError(error: String)
    }

    //監聽
    private var listener: MediaRecordListener? = null

    //狀態
    private var recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_INITIAL

    /**
     * 聲道
     * https://developer.android.com/reference/android/media/MediaRecorder#setAudioChannels(int)
     * int：音頻通道數。通常為1（單聲道）或2（立體聲）。
     */
    private var defaultAudioChannels: Int = 1

    /**
     * 定義音頻源
     * https://developer.android.com/reference/android/media/MediaRecorder.AudioSource
     */
    private var defaultAudioSource: Int = MediaRecorder.AudioSource.MIC

    /**
     * 定義輸出格式
     * https://developer.android.com/reference/android/media/MediaRecorder.OutputFormat
     */
    private var defadultOutputFormat: Int = MediaRecorder.OutputFormat.MPEG_4

    /**
     * 定義音頻編碼
     * https://developer.android.com/reference/android/media/MediaRecorder.AudioEncoder
     */
    private var defaultAudioEncoder: Int = MediaRecorder.AudioEncoder.AAC

    /**
     * 設置錄製的音頻編碼比特率 default = 96khz
     * https://developer.android.com/reference/android/media/MediaRecorder#setAudioEncodingBitRate(int)
     */
    private var defaultAudioEncodingBitRate = 96000

    /**
     * 設置錄製的音頻採樣率 default = 44.1khz
     * https://developer.android.com/reference/android/media/MediaRecorder#setAudioSamplingRate(int)
     */
    private var defaultAudioSamplingRate = 44100

    /**
     * MediaRecord
     */
    private var recorder: MediaRecorder? = null

    /**
     * 檔案名
     */
    private var recordFileName: String = ""

    /**
     * 檔案路徑
     */
    private var recordFilePath: String = ""

    /**
     * 檔案
     */
    private var recordFile: File? = null


    //設定 聲道
    fun setAudioChannels(AudioChannels: Int): MediaRecordManager {
        if (AudioChannels == 1 || AudioChannels == 2) {
            defaultAudioChannels = AudioChannels
        }
        return instance
    }

    //設定 定義音頻源
    fun setAudioSource(AudioSource: Int): MediaRecordManager {
        defaultAudioSource = AudioSource
        return instance
    }

    //設定 定義輸出格式
    fun setOutputFormat(OutputFormat: Int): MediaRecordManager {
        defadultOutputFormat = OutputFormat
        return instance
    }

    //設定 定義音頻編碼
    fun setAudioEncoder(AudioEncoder: Int): MediaRecordManager {
        defaultAudioEncoder = AudioEncoder
        return instance
    }

    //設定 設置錄製的音頻編碼比特率
    fun setAudioEncodingBitRate(AudioEncodingBitRate: Int): MediaRecordManager {
        defaultAudioEncodingBitRate = AudioEncodingBitRate
        return instance
    }

    //設定 設置錄製的音頻採樣率
    fun setAudioSamplingRate(AudioSamplingRate: Int): MediaRecordManager {
        defaultAudioSamplingRate = AudioSamplingRate
        return instance
    }

    //設定 監聽
    fun setAudioSamplingRate(listener: MediaRecordListener): MediaRecordManager {
        this.listener = listener
        return instance
    }

    //設定 檔名
    fun setRecordFileName(context: Context): MediaRecordManager {
        recordFile = MediaHelper(context).getOutputMediaFile(MediaHelper.MEDIA_TYPE_MUSIC)
        return instance
    }


    //建立錄製音頻實體 [初始化]
    fun createMediaRecord(): MediaRecordManager {
        recorder = MediaRecorder()
        recorder!!.setAudioChannels(defaultAudioChannels)
        recorder!!.setAudioSource(defaultAudioSource)
        recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_INITIALIZED
        recorder!!.setOutputFormat(defadultOutputFormat)
        recorder!!.setAudioEncoder(defaultAudioEncoder)
        recorder!!.setAudioEncodingBitRate(defaultAudioEncodingBitRate)
        recorder!!.setAudioSamplingRate(defaultAudioSamplingRate)
        //輸出檔名
        Log.d("recordFile", "path: ${recordFile?.path}")
        recorder!!.setOutputFile(recordFile?.path)
        recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_DATA_SOURCE_CONFIGURED
        return instance
    }

    //準備
    fun prepareRecord() {
        if (recordStatus != MediaRecordConstants.MEDIA_RECORD_STATUS_DATA_SOURCE_CONFIGURED) {
            return
        }
        try{
            recorder?.prepare()
        }catch (e:IllegalStateException){
            Log.d(
                "prepareRecord",
                "IllegalStateException preparing MediaRecorder: " + e.message
            )
            recorder?.reset()
            recorder?.release()
            recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_RELEASE
        }catch (e: IOException){
            Log.d(
                "prepareRecord",
                "IOException preparing MediaRecorder: " + e.message
            )
            recorder?.reset()
            recorder?.release()
            recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_RELEASE
        }
        recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_PREPAR
    }

    fun startRecord() {
        Log.d("MediaRecordManager","startRecord")
//        MediaPrepareTask().execute(null,null,null)
        if (recordStatus != MediaRecordConstants.MEDIA_RECORD_STATUS_PREPAR) {
            return
        }
        recorder?.start()

        recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_START
    }

    fun stopRecord() {
        Log.d("MediaRecordManager","stopRecord")
        if (recordStatus != MediaRecordConstants.MEDIA_RECORD_STATUS_START) {
            return
        }
        try {
            recorder?.stop()
            recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_STOP
            recorder?.reset()
            recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_INITIAL
            Log.d("stopRecord", "length:${recordFile?.length()}")
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            Log.d("stopRecord", "${e.message}")
        } catch (e: RuntimeException) {
            e.printStackTrace()
            Log.d("stopRecord", "${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("stopRecord", "${e.message}")
        }
    }

    //繼續下一個動作
    fun continueToTheNextAction() {
        when (recordStatus) {
            MediaRecordConstants.MEDIA_RECORD_STATUS_INITIAL -> createMediaRecord()
            MediaRecordConstants.MEDIA_RECORD_STATUS_DATA_SOURCE_CONFIGURED -> prepareRecord()
            MediaRecordConstants.MEDIA_RECORD_STATUS_PREPAR -> startRecord()
            MediaRecordConstants.MEDIA_RECORD_STATUS_START -> stopRecord()
        }
    }

    //取得狀態
    fun getStatus(): String {
        return recordStatus
    }

    /**
     * Asynchronous task for preparing the [android.media.MediaRecorder] since it's a long blocking
     * operation.
     */
    internal class MediaPrepareTask : AsyncTask<Void?, Void?, Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean {
            // initialize video camera
            if (instance.recordStatus == MediaRecordConstants.MEDIA_RECORD_STATUS_PREPAR) {
                return false
            }
            instance.recorder?.start()
            instance.recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_START
            return true
        }

        override fun onPostExecute(result: Boolean) {
            Log.d("MediaPrepareTask", "result: $result")
            if (!result) {

            }
        }
    }
}