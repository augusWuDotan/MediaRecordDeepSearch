package com.augus.mediarecorddeepsearch.lib

import android.media.MediaRecorder
import java.io.File

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
    private var recordFileName: String = "test.4ma"

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
        recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_DATA_SOURCE_CONFIGURED
        return instance
    }

    //準備
    fun prepareMediaRecord() {
        if (recordStatus == MediaRecordConstants.MEDIA_RECORD_STATUS_DATA_SOURCE_CONFIGURED) recorder?.prepare()
    }


}