package com.shazycode.learnio.model.repositories

import android.net.Uri
import com.shazycode.learnio.model.data.CloudinaryUploadHelper

class StorageRepository {
    fun uploadFile(uri: Uri, onComplete: (Boolean, String?) -> Unit) {
        CloudinaryUploadHelper().uploadFile(uri, onComplete)
    }
}