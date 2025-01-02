package com.shazycode.learnio

import android.app.Application
import com.shazycode.learnio.model.data.CloudinaryUploadHelper.Companion.initializeCloudinary

class LearnIOApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeCloudinary(this)
    }


}