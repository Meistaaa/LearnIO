package com.shazycode.learnio.model.data


import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback



class CloudinaryUploadHelper() {
    companion object {
        fun initializeCloudinary(context: Context) {
            val config = mapOf(
                "cloud_name" to "dibyuxyut",  // Replace with your Cloudinary cloud name
                "api_key" to "433875515141438",       // Replace with your Cloudinary API key
                "api_secret" to "u68VgTknQEmVnUwxPYXFAHxFy5U" // Replace with your Cloudinary API secret
            )
            MediaManager.init(context, config)
        }
    }


    fun uploadFile(
        uri: Uri,
        onComplete: (Boolean, String?) -> Unit
    ) {
        // Use the upload method that accepts a Uri
        MediaManager.get().upload(uri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d("Cloudinary", "Upload started: $requestId")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    val progress = (bytes.toDouble() / totalBytes * 100).toInt()
                    Log.d("Cloudinary", "Upload progress: $progress%")
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val fileUrl = (resultData["url"] as? String)?.replace("http://", "https://")
                    if (fileUrl != null) {
                        Log.d("Cloudinary", "Upload successful. URL: $fileUrl")
                        onComplete(true, fileUrl)
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("Cloudinary", "Upload failed: ${error?.description}")
                    onComplete(false, error?.description)
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    Log.e("Cloudinary", "Upload rescheduled")
                }
            }).dispatch()
    }
}

