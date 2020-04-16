package com.augus.mediarecorddeepsearch.lib

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MediaHelper(val context: Context) {
    private var tag: String = context.packageName

    companion object {
        const val MEDIA_TYPE_VIDEO = 1
        const val MEDIA_TYPE_MUSIC = 2
    }

    fun getOutputMediaFile(type: Int): File? {

        val mediaStorageDir: File = File(context.getFilesDir(), "test")

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(tag, "failed to create directory")
                return null
            }
        }

        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())
        return when (type) {
            MEDIA_TYPE_MUSIC -> File("${mediaStorageDir.path}${File.separator}IMG_${timeStamp}.m4a")
            MEDIA_TYPE_VIDEO -> File("${mediaStorageDir.path}${File.separator}VID_${timeStamp}.mp4")
            else -> null
        }
    }
}