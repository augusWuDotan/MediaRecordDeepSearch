package com.augus.mediarecorddeepsearch.lib

import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException
import java.lang.Exception


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
    fun setRecordFileName(name: String, path: String): MediaRecordManager {
        recordFileName = name
        recordFilePath = path
        return instance
    }


    //建立錄製音頻實體 [初始化]
    fun createMediaRecord(): MediaRecordManager {
        val child: File = File(Environment.getDataDirectory().absolutePath + "/" + recordFileName)
        val directory: File = child.parentFile
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Path to file could not be created.")
        }

//        //檔案
//        var file: File = File(Environment.getDataDirectory().absolutePath, recordFileName)
//        Log.d("createMediaRecord", "absolutePath: ${file.path}")
//        /**
//         * 檔案是否存在
//         */
//        if (!file.exists()) {
//            Log.d("test","create success : ${file.createNewFile()}")
//        }

        recorder = MediaRecorder()
        recorder!!.setAudioChannels(defaultAudioChannels)
        recorder!!.setAudioSource(defaultAudioSource)
        recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_INITIALIZED
        recorder!!.setOutputFormat(defadultOutputFormat)
        recorder!!.setAudioEncoder(defaultAudioEncoder)
        recorder!!.setAudioEncodingBitRate(defaultAudioEncodingBitRate)
        recorder!!.setAudioSamplingRate(defaultAudioSamplingRate)
        recorder!!.setOnErrorListener(null);
        recorder!!.setOnInfoListener(null);
        recorder!!.setPreviewDisplay(null);
        //輸出檔名
        recorder!!.setOutputFile(recordFilePath + "/" + recordFileName)
        recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_DATA_SOURCE_CONFIGURED
        return instance
    }

    //準備
    fun prepareRecord() {
        if (recordStatus != MediaRecordConstants.MEDIA_RECORD_STATUS_DATA_SOURCE_CONFIGURED) {
            return
        }
        recorder?.prepare()
        recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_PREPAR
    }

    fun startRecord() {
        if (recordStatus == MediaRecordConstants.MEDIA_RECORD_STATUS_PREPAR) {
            return
        }
        recorder?.start()
        recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_START
    }

    fun stopRecord() {
        if (recordStatus == MediaRecordConstants.MEDIA_RECORD_STATUS_START) {
            return
        }
        try{
            recorder?.stop()
            recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_STOP
            recorder?.reset()
            recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_INITIAL
        } catch (e:IllegalStateException ) {
            e.printStackTrace()
            Log.d("stopRecord","${e.message}")
        } catch (e: RuntimeException ) {
            e.printStackTrace()
            Log.d("stopRecord","${e.message}")
        } catch (e: Exception ) {
            e.printStackTrace()
            Log.d("stopRecord","${e.message}")
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
}