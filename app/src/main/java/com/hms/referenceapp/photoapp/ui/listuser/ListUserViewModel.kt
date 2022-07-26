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
import com.hms.referenceapp.photoapp.data.model.user
import com.hms.referenceapp.photoapp.data.repository.CloudDbRepository
import com.hms.referenceapp.photoapp.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListUserViewModel @Inject constructor(
    private val cloudDbRepository: CloudDbRepository
) : BaseViewModel() {

    private val _listUserUiState = MutableStateFlow(ListUserUiState.initial())
    val listUserUiState: StateFlow<ListUserUiState> get() = _listUserUiState.asStateFlow()

    fun getUsers() {
        cloudDbRepository.getUsers()
        viewModelScope.launch {
            cloudDbRepository.cloudDbUserResponse.collect {
                handleGetUserListStatus(it)
            }
        }
    }

    private fun handleGetUserListStatus(result: List<user>?) {
            result?.let {
            val userUiModelList = result.map {
                it.toUserSelectUiModel()
            }

            _listUserUiState.update { currentUserListUiState ->
                currentUserListUiState.copy(
                    savedUserList = userUiModelList
                )
            }
        }
    }

    private fun user.toUserSelectUiModel(): UserSelectUiModel {
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