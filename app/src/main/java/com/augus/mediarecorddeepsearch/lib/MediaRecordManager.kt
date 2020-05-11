package com.augus.mediarecorddeepsearch.lib

import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.floor
import kotlin.math.log10


class MediaRecordManager {
    companion object {
        val instance = MediaRecordHolder.initManager
        var mEMA: Double = 0.0
    }

    object MediaRecordHolder {
        val initManager = MediaRecordManager()
    }

    interface MediaRecordListener {
        //準備成功
        fun prepareSucess()

        //錄音提示開始
        fun recordStart()

        //錄音回應分貝量
        fun showDecibel(decibelPercentage: Float)

        //錄音成功
        fun recordSuccess(file: File?)

        //錄音提示已關閉
        fun recordStop()

        //錄音錯誤
        fun recordError(error: String)

        //錄音檔案回收成功
        fun recordRecycleSuccess(isSuccess: Boolean)
    }

    //監聽
    private var listener: MediaRecordListener? = null

    //錄音當下狀態
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

    //介面實體
    private var mContext: Context? = null

    /**
     * 檔案
     */
    private var recordFile: File? = null

    //取樣數
    private val numberOfSamples = 3

    //初始分貝收集
    private var initialDecibelCollection: MutableList<Double> = mutableListOf()

    //敏感依據低分貝 低分貝
    private var baseLowDecibel: Double = 0.0

    //最高分貝
    private val highDecibel: Double = 90.30873362283398

    //線程池
    private val pool: ScheduledExecutorService? = Executors.newScheduledThreadPool(1)

    //須執行線程內容
    internal class EchoServer : Runnable {
        override fun run() {
            try {
                if (instance.recordStatus == MediaRecordConstants.MEDIA_RECORD_STATUS_START) {
                    val ratio: Double = instance.recorder?.maxAmplitude!!.toDouble()
                    val ratioDb = 20 * log10(ratio)
                    Log.d("voice", "ratio:$ratio,ratioDb:$ratioDb")

                    if (instance.initialDecibelCollection.size >= instance.numberOfSamples) {
                        if (instance.baseLowDecibel == 0.0) {
                            instance.initialDecibelCollection.sort()
                            instance.baseLowDecibel =
                                instance.initialDecibelCollection[floor(instance.initialDecibelCollection.size / 2.0).toInt()]
                        } else {
                            instance.listener?.showDecibel(
                                if (ratioDb > instance.baseLowDecibel) ((ratioDb - instance.baseLowDecibel) / (instance.highDecibel - instance.baseLowDecibel)).toFloat()
                                else 0f
                            )
                        }
                    } else {
                        if (ratio > 0) {
                            instance.initialDecibelCollection.add(ratioDb)
                            instance.listener?.showDecibel(0f)
                        }
                    }
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    //線程實體
    private var mDecibelThread: ScheduledFuture<*>? = null

    //設定 聲道
    fun setAudioChannels(AudioChannels: Int): MediaRecordManager {
        if (AudioChannels == 1 || AudioChannels == 2) {
            defaultAudioChannels = AudioChannels
            recorder?.setAudioChannels(defaultAudioChannels)
        }
        return instance
    }

    //設定 定義音頻源
    fun setAudioSource(AudioSource: Int): MediaRecordManager {
        defaultAudioSource = AudioSource
        recorder?.setAudioSource(defaultAudioSource)
        return instance
    }

    //設定 定義輸出格式
    fun setOutputFormat(OutputFormat: Int): MediaRecordManager {
        defadultOutputFormat = OutputFormat
        recorder?.setOutputFormat(defadultOutputFormat)
        return instance
    }

    //設定 定義音頻編碼
    fun setAudioEncoder(AudioEncoder: Int): MediaRecordManager {
        defaultAudioEncoder = AudioEncoder
        recorder?.setAudioEncoder(defaultAudioEncoder)
        return instance
    }

    //設定 設置錄製的音頻編碼比特率
    fun setAudioEncodingBitRate(AudioEncodingBitRate: Int): MediaRecordManager {
        defaultAudioEncodingBitRate = AudioEncodingBitRate
        recorder?.setAudioEncodingBitRate(defaultAudioEncodingBitRate)
        return instance
    }

    //設定 設置錄製的音頻採樣率
    fun setAudioSamplingRate(AudioSamplingRate: Int): MediaRecordManager {
        defaultAudioSamplingRate = AudioSamplingRate
        recorder?.setAudioSamplingRate(defaultAudioSamplingRate)
        return instance
    }

    //設定 監聽
    fun setRecorderListener(listener: MediaRecordListener): MediaRecordManager {
        this.listener = listener
        return instance
    }

    //建立錄製音頻實體 [初始化]
    fun createMediaRecord(context: Context): MediaRecordManager {
        mContext = context
        recorder = MediaRecorder()
        recorder?.setAudioChannels(defaultAudioChannels)
        recorder?.setAudioSource(defaultAudioSource)
        recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_INITIALIZED
        recorder?.setOutputFormat(defadultOutputFormat)
        recorder?.setAudioEncoder(defaultAudioEncoder)
        recorder?.setAudioEncodingBitRate(defaultAudioEncodingBitRate)
        recorder?.setAudioSamplingRate(defaultAudioSamplingRate)
        //輸出檔名
        recordFile = MediaHelper(context).getOutputMediaFile(MediaHelper.MEDIA_TYPE_MUSIC)
        Log.d("recordFile", "path: ${recordFile?.path}")
        recorder?.setOutputFile(recordFile?.path)
        recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_DATA_SOURCE_CONFIGURED
        return instance
    }

    //準備錄音 MEDIA_RECORD_STATUS_DATA_SOURCE_CONFIGURED的前一個動作
    fun prepareRecord(): MediaRecordManager {
        if (recordStatus != MediaRecordConstants.MEDIA_RECORD_STATUS_DATA_SOURCE_CONFIGURED) {
            Log.d("MediaRecordManager", "prepareRecord->recordStatus:$recordStatus")
            return instance
        }
        try {
            recorder?.prepare()
            //監聽回報已經準備好
            listener?.prepareSucess()
            //改變錄音狀態 PREPAR
            recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_PREPAR

        } catch (e: IllegalStateException) {
            Log.d(
                "prepareRecord",
                "IllegalStateException preparing MediaRecorder: " + e.message
            )
            listener?.recordError(e.message!!)
            restartMediaRecorder()
        } catch (e: IOException) {
            Log.d(
                "prepareRecord",
                "IOException preparing MediaRecorder: " + e.message
            )
            listener?.recordError(e.message!!)
            restartMediaRecorder()
        } finally {
            return instance
        }
    }

    //region 開始錄音
    fun startRecord() {
        if (recordStatus != MediaRecordConstants.MEDIA_RECORD_STATUS_PREPAR) {
            Log.d("MediaRecordManager", "startRecord->recordStatus:$recordStatus")
            return
        }
        try {
            Log.d("MediaRecordManager", "startRecord")
            //開始錄音
            recorder?.start()
            //監聽回報已經開始錄音
            listener?.recordStart()
            //建立監聽分貝線程
            mDecibelThread =
                pool?.scheduleWithFixedDelay(EchoServer(), 0, 100, TimeUnit.MILLISECONDS)
            //改變錄音狀態 START
            recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_START
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            Log.d("startRecord", "${e.message}")
            listener?.recordError(e.message!!)
            restartMediaRecorder()
        } catch (e: RuntimeException) {
            e.printStackTrace()
            Log.d("startRecord", "${e.message}")
            listener?.recordError(e.message!!)
            restartMediaRecorder()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("startRecord", "${e.message}")
            listener?.recordError(e.message!!)
            restartMediaRecorder()
        }
    }
    //endregion

    //region 結束錄音
    fun stopRecord() {
        if (recordStatus != MediaRecordConstants.MEDIA_RECORD_STATUS_START) {
            Log.d("MediaRecordManager", "stopRecord->recordStatus:$recordStatus")
            return
        }
        try {
            Log.d("MediaRecordManager", "stopRecord")
            //監聽分貝線程停止
            mDecibelThread?.cancel(true)
            mDecibelThread = null
            //錄音器呼叫停止
            recorder?.stop()
            //監聽回報已經停止錄音
            listener?.recordStop()
            //改變錄音狀態 STOP
            recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_STOP
            //重置錄音器
            recorder?.reset()
            //改變錄音狀態 INITIAL
            recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_INITIAL
            //監聽回報錄音檔案 file
            listener?.recordSuccess(recordFile)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            Log.d("stopRecord", "${e.message}")
            listener?.recordError(e.message!!)
            restartMediaRecorder()
        } catch (e: RuntimeException) {
            e.printStackTrace()
            Log.d("stopRecord", "${e.message}")
            listener?.recordError(e.message!!)
            restartMediaRecorder()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("stopRecord", "${e.message}")
            listener?.recordError(e.message!!)
            restartMediaRecorder()
        }
    }
    //endregion

    //重新生成一個錄音器
    private fun restartMediaRecorder() {
        Log.d("MediaRecordManager", "restartMediaRecorder")
        recorder?.reset()
        recorder?.release()
        recordStatus = MediaRecordConstants.MEDIA_RECORD_STATUS_RELEASE
        mContext?.let {
            recycleRecorderFile()
            createMediaRecord(it)
        }
    }

    //回收檔案
    fun recycleRecorderFile() {
        Log.d("MediaRecordManager", "recycleRecorderFile")
        val isSuccess: Boolean = recordFile?.delete()!!
        Log.d("MediaRecordManager", "isSuccess:$isSuccess")
        if (isSuccess) {
            listener?.recordRecycleSuccess(isSuccess)
        } else {
            listener?.recordRecycleSuccess(false)
        }
    }

    //取得狀態
    fun getRecordStatus(): String {
        return recordStatus
    }

}
