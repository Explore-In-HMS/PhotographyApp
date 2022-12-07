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
import com.hms.referenceapp.photoapp.data.model.User
import com.hms.referenceapp.photoapp.data.repository.CloudDbRepository
import com.hms.referenceapp.photoapp.ui.base.BaseViewModel
import com.hms.referenceapp.photoapp.util.Constant.FILE_ID
import com.hms.referenceapp.photoapp.util.Constant.RECEIVER_ID
import com.hms.referenceapp.photoapp.util.Constant.SENDER_ID
import com.huawei.agconnect.auth.AGConnectAuth
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val hashmapSharedPeople = HashMap<String, ArrayList<User>>()
    private val hashmapFilesSharedWithYouPeople = HashMap<String, ArrayList<User>>()
    private val receiverFileList = mutableListOf<PhotoDetails>()

    init {
        unRegisterToListen()
        observeCloudDbZone()
        observeRealTimeFilesYouSharedResponse()
        observeRealTimeSharedFilesWithYouResponse()
        observeRealTimeSharedFilesWithYouReceiversResponse()
    }

    fun filterFilesYouSharedList(photoDetailList: List<PhotoDetails>): List<SharePhotoModel> {
        photoDetailList.forEach {
            hashmapSharedPeople[it.fileId] = arrayListOf()
        }
        photoDetailList.forEach {
            val sharedUser = User()
            sharedUser.id = it.receiverId.toLong()
            sharedUser.name = it.receiverName
            hashmapSharedPeople[it.fileId]?.add(sharedUser)
        }

        val sortedPhotoDetailList = photoDetailList.sortedBy {
            it.id
        }

        return sortedPhotoDetailList.distinctBy { it.fileId }.map {
            SharePhotoModel(
                id = it.id,
                fileId = it.fileId,
                title = it.fileName,
                description = it.fileDesc,
                sharedPersonCount = it.numberOfPeopleShared,
                true
            )
        }
    }

    fun filterSharedFilesWithYouList(photoDetailList: List<PhotoDetails>): List<SharePhotoModel> {
        photoDetailList.forEach { photoDetails ->
            viewModelScope.launch {
                cloudDbRepository.cloudDbZoneFlow.collect {
                    if (it != null && hashmapFilesSharedWithYouPeople[photoDetails.fileId.toString()] == null) {
                        cloudDbRepository.addSubscriptionForSharedFilesWithYouReceivers(
                            it,
                            FILE_ID,
                            photoDetails.fileId
                        )
                    }
                }
            }
        }
        return photoDetailList.map {
            SharePhotoModel(
                id = it.id,
                fileId = it.fileId,
                title = it.fileName,
                description = it.fileDesc,
                sharedPersonCount = ((it.numberOfPeopleShared.toInt() + 1).toString()),
                false
            )
        }
    }

    private fun setReceivedUsers(photoDetailList: List<PhotoDetails>) {
        photoDetailList.forEach {
            hashmapFilesSharedWithYouPeople[it.fileId] = arrayListOf()
        }
        for (item in photoDetailList) {
            val sharedUser = User()
            sharedUser.id = item.senderId.toLong()
            sharedUser.name = item.senderName
            hashmapFilesSharedWithYouPeople[item.fileId]?.add(sharedUser)
            break
        }
        photoDetailList.forEach {
            val sharedUser = User()
            sharedUser.id = it.receiverId.toLong()
            sharedUser.name = it.receiverName
            hashmapFilesSharedWithYouPeople[it.fileId]?.add(sharedUser)
        }
    }

    fun prepareFileData(fileInformationModel: FileInformationModel?) {
        var milliSecond = System.currentTimeMillis().toInt()
        val fileIdWithMillisecond = "fileId_${milliSecond}"

        fileInformationModel?.userList?.forEach { checkedUser ->
            PhotoDetails().apply {
                id = milliSecond
                senderId = agConnectUser.currentUser.uid
                senderName = agConnectUser.currentUser.displayName
                receiverId = checkedUser.user.unionId
                receiverName = checkedUser.user.name
                fileId = fileIdWithMillisecond
                fileName = fileInformationModel.title
                fileDesc = fileInformationModel.description
                numberOfPeopleShared = fileInformationModel.numberOfPeopleShared
            }.also {
                saveFileWithPersonToCloud(it)
                milliSecond += 1
            }
        }
    }

    fun deleteSharedFile(fileId: Int, sharedPersonCount: Int) {
        for (i in 0 until sharedPersonCount) {
            cloudDbRepository.deleteSharedFile(fileId + i)
        }
    }

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

    fun getSharedPeopleFromFileId(fileId: String): ArrayList<User>? {
        return hashmapSharedPeople[fileId]
    }

    fun getSharedWithYouPeopleFromFileId(fileId: String): ArrayList<User>? {
        return hashmapFilesSharedWithYouPeople[fileId]
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

    private fun observeRealTimeSharedFilesWithYouReceiversResponse() {
        viewModelScope.launch {
            cloudDbRepository.sharedFilesWithYouReceiverResponse.collect {
                handleSharedFilesWithYouReceiverListStatus(it)
            }
        }
    }

    private fun observeCloudDbZone() {
        viewModelScope.launch {
            cloudDbRepository.cloudDbZoneFlow.collect {
                if (it != null) {
                    cloudDbRepository.addSubscriptionForFilesYouShared(
                        it,
                        SENDER_ID,
                        agConnectUser.currentUser.uid
                    )

                    cloudDbRepository.addSubscriptionForSharedFilesWithYou(
                        it,
                        RECEIVER_ID,
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

    private fun handleSharedFilesWithYouReceiverListStatus(result: MutableList<PhotoDetails>?) {
        receiverFileList.clear()
        result?.forEach {
            receiverFileList.add(it)
        }

        if (result != null) {
            _shareImageUiState.update { currentShareImageUiState ->
                currentShareImageUiState.copy(
                    sharedFilesWithYouReceiverList = receiverFileList
                )
            }
        }
        setReceivedUsers(receiverFileList)
    }

    private fun unRegisterToListen() {
        cloudDbRepository.unRegisterToListen()
    }
}