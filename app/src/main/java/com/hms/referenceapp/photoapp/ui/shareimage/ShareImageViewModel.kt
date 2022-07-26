/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.shareimage

import androidx.lifecycle.viewModelScope
import com.hms.referenceapp.photoapp.common.Result
import com.hms.referenceapp.photoapp.data.model.FileInformationModel
import com.hms.referenceapp.photoapp.data.model.PhotoDetails
import com.hms.referenceapp.photoapp.data.repository.CloudDbRepository
import com.hms.referenceapp.photoapp.ui.base.BaseViewModel
import com.huawei.agconnect.auth.AGConnectAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShareImageViewModel @Inject constructor(
    private val cloudDbRepository: CloudDbRepository,
    private val agConnectUser: AGConnectAuth
) : BaseViewModel() {

    private val _shareImageUiState = MutableStateFlow(ShareImageUiState.initial())
    val shareImageUiState: StateFlow<ShareImageUiState> get() = _shareImageUiState.asStateFlow()

    init {
        unRegisterToListen()
        observeCloudDbZone()
        observeRealTimeFilesYouSharedResponse()
        observeRealTimeSharedFilesWithYouResponse()
    }

    fun filterFilesYouSharedList(photoDetailList: List<PhotoDetails>): List<SharePhotoModel> {
        return photoDetailList.distinctBy {  it.file_id }.map {
            SharePhotoModel(
                id = it.id,
                fileId = it.file_id,
                title = it.file_name,
                description = it.file_desc,
                sharedPersonCount = it.number_of_people_shared
            )
        }
    }

    fun filterSharedFilesWithYouList(photoDetailList: List<PhotoDetails>): List<SharePhotoModel> {
        return photoDetailList.map {
            SharePhotoModel(
                id = it.id,
                fileId = it.file_id,
                title = it.file_name,
                description = it.file_desc,
                sharedPersonCount = it.number_of_people_shared
            )
        }
    }

    @ExperimentalCoroutinesApi
    fun prepareFileData(fileInformationModel: FileInformationModel?) {

        var milliSecond = System.currentTimeMillis().toInt()
        val fileId = "fileId_${milliSecond}"

        fileInformationModel?.userList?.forEach { checkedUser ->
            PhotoDetails().apply {
                id = milliSecond
                sender_id = agConnectUser.currentUser.uid
                sender_name = agConnectUser.currentUser.displayName
                receiver_id = checkedUser.user.unionId
                receiver_name = checkedUser.user.name
                file_id = fileId
                file_name = fileInformationModel.title
                file_desc = fileInformationModel.description
                number_of_people_shared = fileInformationModel.numberOfPeopleShared
            }.also {
                saveFileWithPersonToCloud(it)
                milliSecond += 1
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun saveFileWithPersonToCloud(fileWithPersonList: PhotoDetails) {
        viewModelScope.launch {
            cloudDbRepository.saveToCloudDB(fileWithPersonList).collect {
                handleFileWithPersonSaveStatus(it)
            }
        }
    }

    private fun handleFileWithPersonSaveStatus(result: Result<Boolean>) {
        when (result) {
            is Result.Error -> setErrorState(result.exception)
            Result.Loading -> setLoadingState()
            is Result.Success -> setSavedFileWithPersonState()
        }
    }

    private fun setErrorState(exception: Exception) {
        _shareImageUiState.update { currentShareImageUiState ->
            val errorMessage =
                currentShareImageUiState.error + exception.localizedMessage.orEmpty()
            currentShareImageUiState.copy(error = errorMessage, loading = false)
        }
    }

    private fun setLoadingState() {
        _shareImageUiState.update { currentShareImageUiState ->
            currentShareImageUiState.copy(loading = true)
        }
    }

    private fun setSavedFileWithPersonState() {
        _shareImageUiState.update { currentShareImageUiState ->
            val isFileWithPersonSaved = currentShareImageUiState.isSavedFilesWithPerson + true
            currentShareImageUiState.copy(
                isSavedFilesWithPerson = isFileWithPersonSaved,
                loading = false
            )
        }
    }

    fun savedFileWithPerson() {
        _shareImageUiState.update {
            it.copy(isSavedFilesWithPerson = emptyList())
        }
    }

    fun errorShown() {
        _shareImageUiState.update {
            it.copy(error = emptyList())
        }
    }

    private fun observeRealTimeFilesYouSharedResponse() {
        viewModelScope.launch {
            cloudDbRepository.filesYouSharedResponse.collect {
                handleFilesYouSharedListStatus(it)
            }
        }
    }

    private fun observeRealTimeSharedFilesWithYouResponse() {
        viewModelScope.launch {
            cloudDbRepository.sharedFilesWithYouResponse.collect {
                handleSharedFilesWithYouListStatus(it)
            }
        }
    }

    private fun observeCloudDbZone() {
        viewModelScope.launch {
            cloudDbRepository.cloudDbZoneFlow.collect {
                if (it != null) {
                    cloudDbRepository.addSubscriptionForFilesYouShared(
                        it,
                        "sender_id",
                        agConnectUser.currentUser.uid
                    )

                    cloudDbRepository.addSubscriptionForSharedFilesWithYou(
                        it,
                        "receiver_id",
                        agConnectUser.currentUser.uid
                    )
                }
            }
        }
    }

    private fun handleFilesYouSharedListStatus(result: List<PhotoDetails>?) {
        if (result != null) {
            _shareImageUiState.update { currentShareImageUiState ->
                currentShareImageUiState.copy(
                    filesYouSharedList = result
                )
            }
        }
    }

    private fun handleSharedFilesWithYouListStatus(result: List<PhotoDetails>?) {
        if (result != null) {
            _shareImageUiState.update { currentShareImageUiState ->
                currentShareImageUiState.copy(
                    sharedFilesWithYouList = result
                )
            }
        }
    }

    private fun unRegisterToListen() {
        cloudDbRepository.unRegisterToListen()
    }
}