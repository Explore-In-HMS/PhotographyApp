/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.shareimagedetail

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hms.referenceapp.photoapp.common.Result
import com.hms.referenceapp.photoapp.data.model.Photos
import com.hms.referenceapp.photoapp.data.repository.CloudDbRepository
import com.hms.referenceapp.photoapp.ui.base.BaseViewModel
import com.hms.referenceapp.photoapp.ui.shareimage.SharePhotoModel
import com.hms.referenceapp.photoapp.util.ext.toBitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject


@HiltViewModel
class ShareImageDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cloudDbRepository: CloudDbRepository,
) : BaseViewModel() {


    private val selectedPhotos = mutableListOf<Bitmap>()

    private val _sharePhotoUiState = MutableStateFlow(SharePhotoUiState.initial())
    val sharePhotoUiState get() = _sharePhotoUiState.asStateFlow()

    init {
        val sharePhotoModel =
            ShareImageDetailFragmentArgs.fromSavedStateHandle(savedStateHandle).sharePhotoModel
        setSharedPhotosInfo(sharePhotoModel)
    }


    private fun setSharedPhotosInfo(sharePhotoModel: SharePhotoModel) {
        _sharePhotoUiState.update { current ->
            with(sharePhotoModel) {
                current.copy(
                    id = id,
                    fileId = fileId,
                    title = title,
                    description = description,
                    sharedPersonCount = sharedPersonCount
                )
            }
        }

        cloudDbRepository.allSharedPhotosResponse.onEach { result ->
            result.getContentIfNotHandled()?.let {
                when (it) {
                    is Result.Error -> showError(it.exception.localizedMessage.orEmpty())
                    Result.Loading -> showLoading()
                    is Result.Success -> showAlreadySharedPhotos(it.data.map { it.byte_array_of_photo })
                }
            }
        }.launchIn(viewModelScope)

        cloudDbRepository.getSharedPhotos(sharePhotoModel.fileId)
    }

    private fun showAlreadySharedPhotos(sharedPhotos: List<ByteArray>) {
        _sharePhotoUiState.update { current ->
            current.copy(
                photos = current.photos + sharedPhotos.map { convertByteArrayToBitmap(it) },
                error = null,
                loading = false
            )
        }
    }

    fun setSelectedPhoto(selectedPhotoList: List<Uri>, contentResolver: ContentResolver) {
        selectedPhotoList.forEach {
            selectedPhotos.add(it.toBitmap(contentResolver, true))
        }.also {
            _sharePhotoUiState.update { current ->
                current.copy(photos = (current.photos + selectedPhotos).distinct())
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun sharePhotos() {
        if (isUserSelectPhoto()) {
            showError("Please select photo for sharing..")
            return
        }
        viewModelScope.launch {
            showLoading()
            var photoId = System.currentTimeMillis().toInt()
            var errorResultCount = 0
            selectedPhotos.forEach {
                cloudDbRepository.saveToCloudDB(Photos().apply {
                    id = photoId
                    file_id = sharePhotoUiState.value.fileId
                    byte_array_of_photo = convertBitmapToByteArray(it)
                }.also {
                    photoId += 1
                }).collectLatest {
                    if (it is Result.Error) {
                        errorResultCount++
                    }
                }
            }

            if (errorResultCount > 0) {
                showError(message = "$errorResultCount image did not upload. Please try again.")
            } else {
                showSuccess()
            }
        }
    }

    private fun isUserSelectPhoto() = selectedPhotos.isEmpty()

    private fun showLoading() {
        _sharePhotoUiState.update { current ->
            current.copy(loading = true, error = null, isPhotosSharedSuccessuflly = false)
        }
    }

    private fun showError(message: String) {
        _sharePhotoUiState.update { current ->
            current.copy(
                loading = false,
                error = message,
                isPhotosSharedSuccessuflly = false
            )
        }
    }

    private fun showSuccess() {
        _sharePhotoUiState.update { current ->
            current.copy(loading = false, error = null, isPhotosSharedSuccessuflly = true)
        }
    }

    fun errorShown() {
        _sharePhotoUiState.update {
            it.copy(error = null)
        }
    }

    private fun convertByteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private suspend fun convertBitmapToByteArray(bitmap: Bitmap) =
        withContext(Dispatchers.Default) {
            val baos = ByteArrayOutputStream()
            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                100,
                baos
            ) //Quality compression method, here 100 means no compression, store the compressed data in the BIOS
            var options = 100
            while (getImageSize(baos) > 500) {  //Cycle to determine if the compressed image is greater than 2Mb, greater than continue compression
                baos.reset() //Reset the BIOS to clear it
                //First parameter: picture format, second parameter: picture quality, 100 is the highest, 0 is the worst, third parameter: save the compressed data stream
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    options,
                    baos
                ) //Here, the compression options are used to store the compressed data in the BIOS
                options -= 10 //10 less each time
                Log.d("compressed ${bitmap.generationId}", getImageSize(baos).toString())
            }

            Log.d("compressed-last", getImageSize(baos).toString())
            baos.toByteArray()
        }


    private fun getImageSize(baos: ByteArrayOutputStream) = baos.toByteArray().size / 1024
}