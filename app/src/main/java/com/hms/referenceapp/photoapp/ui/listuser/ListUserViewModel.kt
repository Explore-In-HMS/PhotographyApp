/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.listuser

import androidx.lifecycle.viewModelScope
import com.hms.referenceapp.photoapp.data.model.FileInformationModel
import com.hms.referenceapp.photoapp.data.model.UserSelectUiModel
import com.hms.referenceapp.photoapp.data.model.User
import com.hms.referenceapp.photoapp.data.model.UserRelationship
import com.hms.referenceapp.photoapp.data.repository.CloudDbRepository
import com.hms.referenceapp.photoapp.ui.base.BaseViewModel
import com.huawei.agconnect.auth.AGConnectAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListUserViewModel @Inject constructor(
    private val cloudDbRepository: CloudDbRepository,
    agConnectUser: AGConnectAuth
) : BaseViewModel() {

    private val _listUserUiState = MutableStateFlow(ListUserUiState.initial())
    val listUserUiState: StateFlow<ListUserUiState> get() = _listUserUiState.asStateFlow()
    private var currentUserId : String

    init {
        currentUserId = agConnectUser.currentUser.uid.toString()
    }

    fun getUsers() {
        viewModelScope.launch {
            cloudDbRepository.getPendingRequests()
            cloudDbRepository.cloudDbUserRelationResponse.collect{ userRelationList->
                handleGetUserListStatus(userRelationList)
            }
        }
    }

    private fun handleGetUserListStatus(userRelationList: MutableList<UserRelationship>?  ) {
        val friendList = mutableListOf<User>()
        userRelationList?.forEach { userRelation->
            if (userRelation.areFriends == true) {
                if (currentUserId == userRelation.firstUserId){
                    friendList.add(User().apply {
                        id = userRelation.secondUserId.toLong()
                        unionId = userRelation.secondUserId
                        name = userRelation.secondUserName
                    })
                }
                if (currentUserId == userRelation.secondUserId){
                    friendList.add(User().apply {
                        id = userRelation.firstUserId.toLong()
                        unionId = userRelation.firstUserId
                        name = userRelation.firstUserName
                    })
                }
            }
        }

        val userUiModelList = friendList.map {
            it.toUserSelectUiModel()
        }
        _listUserUiState.update { currentUserListUiState ->
            currentUserListUiState.copy(
                savedUserList = userUiModelList
            )
        }
    }

    private fun User.toUserSelectUiModel(): UserSelectUiModel {
        return UserSelectUiModel(this, false)
    }

    fun getSelectedUsers(): List<UserSelectUiModel> {
        return _listUserUiState.value.savedUserList.filter {
            it.isChecked
        }
    }

    fun selectUser(user: UserSelectUiModel) {
        val currentUserList = _listUserUiState.value.savedUserList
        currentUserList.find {
            it == user
        }?.also {
            it.isChecked = user.isChecked
        }
        _listUserUiState.update { currentUserListUiState ->
            currentUserListUiState.copy(
                savedUserList = currentUserList
            )
        }
    }

    fun controlFileInformationModel(fileInformation: FileInformationModel) {
        if (fileInformation.description.isEmpty()) {
            _listUserUiState.update { currentListUserUiState ->
                currentListUserUiState.copy(
                    error = ERROR_DESCRIPTION
                )
            }
            return
        }
        if (fileInformation.title.isEmpty()) {
            _listUserUiState.update { currentListUserUiState ->
                currentListUserUiState.copy(
                    error = ERROR_TITLE
                )
            }
            return
        }
        if (fileInformation.userList.isEmpty()) {
            _listUserUiState.update { currentListUserUiState ->
                currentListUserUiState.copy(
                    error = ERROR_USER_LIST
                )
            }
            return
        }
        _listUserUiState.update { currentListUserUiState ->
            currentListUserUiState.copy(
                shareImageInformationFileTaken = true
            )
        }
    }

    fun errorShown() {
        _listUserUiState.update {
            it.copy(error = null)
        }
    }

    companion object{
        const val ERROR_DESCRIPTION = "Description can not be empty"
        const val ERROR_TITLE = "Title can not be empty"
        const val ERROR_USER_LIST = "You have to select at least one user"
    }
}