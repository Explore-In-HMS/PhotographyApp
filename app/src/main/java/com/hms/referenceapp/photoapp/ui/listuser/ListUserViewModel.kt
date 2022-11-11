/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.listuser

import android.util.Log
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
    var currentUserId : String

    init {
        currentUserId = agConnectUser.currentUser.uid.toString()
    }

    fun getUsers() {
        cloudDbRepository.getUsers()
        viewModelScope.launch {
            cloudDbRepository.cloudDbUserResponse.collect { allUserList->
                cloudDbRepository.getPendingRequests()
                cloudDbRepository.cloudDbUserRelationResponse.collect{ userRelationList->
                    handleGetUserListStatus(allUserList, userRelationList)
                }
            }
        }
    }

    private fun handleGetUserListStatus(userList: MutableList<User>?, userRelationList: MutableList<UserRelationship>?  ) {
            val friendList = mutableListOf<User>()
            // cok kullanıcı oldugu durumda ic ice for efficient bir cozum degil bunun yerine relation tablosunda name alanı oluşturulup ordan name de çekilere yeni user oluşturulabilir bu sayede tüm kullanıcıları çekip kontrol etmeye gerek kalmaz
            userList?.forEach { user ->
                userRelationList?.forEach { userRelation->
                    if (currentUserId == userRelation.firstUserId && user.id.toString() == userRelation.secondUserId && userRelation.areFriends == true){
                        friendList.add(user)
                    }
                    if (currentUserId == userRelation.secondUserId && user.id.toString() == userRelation.firstUserId && userRelation.areFriends == true){
                        friendList.add(user)
                    }
                }
            }


        friendList.let {
            val userUiModelList = friendList.map {
                it.toUserSelectUiModel()
            }

            _listUserUiState.update { currentUserListUiState ->
                currentUserListUiState.copy(
                    savedUserList = userUiModelList
                )
            }
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
        Log.d("TAG", "message : ${getSelectedUsers()}")
    }

    fun controlFileInformationModel(fileInformation: FileInformationModel) {
        if (fileInformation.description.isEmpty()) {
            _listUserUiState.update { currentListUserUiState ->
                currentListUserUiState.copy(
                    error = "description can not be empty"
                )
            }
            return
        }
        if (fileInformation.title.isEmpty()) {
            _listUserUiState.update { currentListUserUiState ->
                currentListUserUiState.copy(
                    error ="title can not be empty"
                )
            }
            return
        }
        if (fileInformation.userList.isEmpty()) {
            _listUserUiState.update { currentListUserUiState ->
                currentListUserUiState.copy(
                    error = "You have to select at least one user"
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
}