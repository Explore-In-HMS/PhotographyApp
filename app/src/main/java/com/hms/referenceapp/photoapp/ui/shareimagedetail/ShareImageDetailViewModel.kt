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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hms.referenceapp.photoapp.R
import com.hms.referenceapp.photoapp.common.Result
import com.hms.referenceapp.photoapp.data.model.ParcelableUser
import com.hms.referenceapp.photoapp.data.model.PhotoDetails
import com.hms.referenceapp.photoapp.data.model.Photos
import com.hms.referenceapp.photoapp.data.repository.CloudDbRepository
import com.hms.referenceapp.photoapp.di.ResourceProvider
import com.hms.referenceapp.photoapp.ui.base.BaseViewModel
import com.hms.referenceapp.photoapp.ui.shareimage.SharePhotoModel
import com.hms.referenceapp.photoapp.util.ext.toBitmap
import com.hms.referenceapp.photoapp.util.ext.toBytes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject


@HiltViewModel
class ShareImageDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cloudDbRepository: CloudDbRepository,
    private val resourceProvider: ResourceProvider
) : BaseViewModel() {

    private val selectedPhotos = mutableListOf<Bitmap>()

    private val _sharePhotoUiState = MutableStateFlow(SharePhotoUiState.initial())
    val sharePhotoUiState get() = _sharePhotoUiState.asStateFlow()

    init {
        with(ShareImageDetailFragmentArgs.fromSavedStateHandle(savedStateHandle)){
            setSharedPhotosInfo(sharePhotoModel, sharedUserList, didIShare)
        }
    }


    private fun setSharedPhotosInfo(sharePhotoModel: SharePhotoModel, sharedUserList: Array<ParcelableUser>, didIShare: Boolean) {
        _sharePhotoUiState.update { current ->
            with(sharePhotoModel) {
                current.copy(
                    id = id,
                    fileId = fileId,
                    title = title,
                    description = description,
                    sharedPersonCount = sharedPersonCount,
                    sharedUserList = sharedUserList.toList(),
                    didIShare = didIShare
                )
            }
        }

        cloudDbRepository.allSharedPhotosResponse.onEach { result ->
            result.getContentIfNotHandled()?.let { it ->
                when (it) {
                    is Result.Error -> showError(it.exception.localizedMessage.orEmpty())
                    is Result.Loading -> showLoading()
                    is Result.Success -> showAlreadySharedPhotos(
                        it.data.map { it.byteArrayOfPhoto },
                        it.data
                    )
                }
            }
        }.launchIn(viewModelScope)

        cloudDbRepository.getSharedPhotos(sharePhotoModel.fileId)
    }

    private fun showAlreadySharedPhotos(
        sharedPhotos: List<ByteArray>,
        updatedPhotos: List<Photos>
    ) {
        _sharePhotoUiState.update { current ->
            current.copy(
                photos = current.photos + sharedPhotos.map { convertByteArrayToBitmap(it) },
                updatedPhotos = current.updatedPhotos + updatedPhotos,
                error = null,
                loading = false
            )
        }
    }

    fun deleteSharedPhotos(deletedPhotos: List<Photos>) {
        deletedPhotos.forEach {
            cloudDbRepository.deletePhotos(it.id)
        }

        cloudDbRepository.deleteSharedPhotosResponse.onEach { result ->
            result.getContentIfNotHandled()?.let { it ->
                when (it) {
                    is Result.Error -> showError(it.exception.localizedMessage.orEmpty())
                    is Result.Loading -> showLoading()
                    is Result.Success -> removeDeletedSharedPhotos(deletedPhotos)
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun removeDeletedSharedPhotos(deletedPhotos: List<Photos>) {
        _sharePhotoUiState.update { current ->
            current.copy(
                updatedPhotos = current.updatedPhotos - deletedPhotos.toSet(),
                error = null,
                loading = false
            )
        }
    }

    fun setSelectedPhoto(selectedPhotoList: List<Uri>, contentResolver: ContentResolver) {
        viewModelScope.launch {
            val newList = arrayListOf<Photos>()
            selectedPhotoList.forEach {
                selectedPhotos.add(it.toBitmap(contentResolver, true))
                newList.add(Photos().apply {
                    id = sharePhotoUiState.value.id
                    fileId = sharePhotoUiState.value.fileId
                    byteArrayOfPhoto = it.toBytes(contentResolver)
                })
            }.also {
                _sharePhotoUiState.update { current ->
                    current.copy(
                        updatedPhotos = current.updatedPhotos + newList
                    )
                }
            }
        }
    }

    fun deleteUserFromSharedFile(fileId: String, receiverId: Long) {
        cloudDbRepository.deleteUserFromSharedFile(fileId, receiverId)
        cloudDbRepository.deleteUserResponse.onEach { result ->
            result.getContentIfNotHandled()?.let { it ->
                when (it) {
                    is Result.Error -> showError(it.exception.localizedMessage.orEmpty())
                    is Result.Loading -> showLoading()
                    is Result.Success -> updateSharedUserList(it.data)
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun updateSharedUserList(deletedUser: PhotoDetails) {
        _sharePhotoUiState.update { current ->
            val newList = current.sharedUserList - listOf(ParcelableUser(deletedUser.receiverId.toLong(), null, deletedUser.receiverName)).toSet()
            current.copy(
                sharedUserList = newList,
                error = null,
                loading = false
            )
        }
    }

    fun sharePhotos() {
        if (isUserSelectPhoto()) {
            showError(resourceProvider.getString(R.string.error_select_photo))
            return
        }
        viewModelScope.launch {
            showLoading()
            var photoId = System.currentTimeMillis().toInt()
            var errorResultCount = 0
            selectedPhotos.forEach {
                cloudDbRepository.saveToCloudDB(Photos().apply {
                    id = photoId
                    fileId = sharePhotoUiState.value.fileId
                    byteArrayOfPhoto = convertBitmapToByteArray(it)
                }.also {
                    photoId += 1
                }).collectLatest {
                    if (it is Result.Error) {
                        errorResultCount++
                    }
                }
            }

            if (errorResultCount > 0) {
                showError(message = errorResultCount.toString() + resourceProvider.getString(R.string.error_did_not_upload))
            } else {
                showSuccess()
            }
        }
    }

    private fun isUserSelectPhoto() = selectedPhotos.isEmpty()

    private fun showLoading() {
        _sharePhotoUiState.update { current ->
            current.copy(loading = true, error = null, isPhotosSharedSuccessfully = false)
        }
    }

    private fun showError(message: String) {
        _sharePhotoUiState.update { current ->
            current.copy(
                loading = false,
                error = message,
                isPhotosSharedSuccessfully = false
            )
        }
    }

    private fun showSuccess() {
        _sharePhotoUiState.update { current ->
            current.copy(loading = false, error = null, isPhotosSharedSuccessfully = true)
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
            }
            baos.toByteArray()
        }


    private fun getImageSize(baos: ByteArrayOutputStream) = baos.toByteArray().size / 1024
}