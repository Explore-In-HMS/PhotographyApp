/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.editimage

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.hms.referenceapp.photoapp.ui.base.BaseViewModel
import com.hms.referenceapp.photoapp.util.Constant
import com.hms.referenceapp.photoapp.util.ImagePathConverter
import com.huawei.hms.image.vision.ImageVision
import com.huawei.hms.image.vision.ImageVisionImpl
import com.huawei.hms.image.vision.bean.ImageVisionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class EditImageViewModel @Inject constructor(
) : BaseViewModel() {

    @Inject
    lateinit var imageVisionFilterAPI: ImageVisionImpl

    private var authJson: JSONObject? = null
    private var result: ImageVisionResult? = null

    private val initialImageKitResponseList: Bitmap? = null
    private val _imageKitResponseFlow = MutableStateFlow(initialImageKitResponseList)
    val imageKitResponseFlow: StateFlow<Bitmap?> get() = _imageKitResponseFlow.asStateFlow()

    // Image Kit Implementation

    fun initImageKitService(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            imageVisionFilterAPI.setVisionCallBack(object : ImageVision.VisionCallBack {
                override fun onSuccess(successCode: Int) {
                    val initCode = // please call init function here
                    Log.i("TAG", "initCode: $initCode")
                }

                override fun onFailure(errorCode: Int) {
                    Log.i("TAG", "initCode: $errorCode")
                }
            })
        }
    }

    fun startFilter(
        intensity: Float,
        compress: Float,
        filterType: Int,
        image: Bitmap?,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val jsonObject = JSONObject()
            val taskJson = JSONObject()

            taskJson.put("intensity", intensity)
            taskJson.put("filterType", filterType)
            taskJson.put("compressRate", compress)
            jsonObject.put("requestId", "1")
            jsonObject.put("taskJson", taskJson)
            jsonObject.put("authJson", authJson)

            result = // filter image with Json object here

            withContext(Dispatchers.Main) {
                _imageKitResponseFlow.value = result?.image
            }
        }
    }

    fun convertToBitmap(contentResolver: ContentResolver, imagePath: String): Bitmap {
        return ImagePathConverter().convertToBitmap(
            contentResolver,
            imagePath
        )
    }

    init {
        try {
            authJson = JSONObject(Constant.jsonString)

        } catch (e: JSONException) {
            Log.e("TAG", "tag exp" + e.message)
        }
    }
}