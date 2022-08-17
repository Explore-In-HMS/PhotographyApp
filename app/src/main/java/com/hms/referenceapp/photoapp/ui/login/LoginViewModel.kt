/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.login

import android.content.Intent
import androidx.lifecycle.viewModelScope
import com.hms.referenceapp.photoapp.common.Result
import com.hms.referenceapp.photoapp.data.model.User
import com.hms.referenceapp.photoapp.data.repository.AuthenticationRepository
import com.hms.referenceapp.photoapp.listeners.IServiceListener
import com.hms.referenceapp.photoapp.ui.base.BaseViewModel
import com.huawei.agconnect.auth.AGConnectUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) : BaseViewModel() {

    private val _loginUiState = MutableStateFlow(LoginUiState.initial())
    val loginUiState: StateFlow<LoginUiState> get() = _loginUiState.asStateFlow()

    @ExperimentalCoroutinesApi
    fun userSignedIn(data: Intent?) {
        authenticationRepository.getSignedInUser(
            data,
            object : IServiceListener<AGConnectUser> {
                override fun onSuccess(successResult: AGConnectUser) {
                    saveUserToCloud(successResult)
                }

                override fun onError(exception: Exception) {
                    setErrorState(exception)
                }
            })
    }

    @ExperimentalCoroutinesApi
    fun saveUserToCloud(
        agcUser: AGConnectUser
    ) {
        val user = User().apply {
            id = agcUser.uid.toLong()
            unionId = agcUser.uid
            name = agcUser.displayName
        }

        viewModelScope.launch {
            authenticationRepository.saveUserToCloud(user).collect {
                handleUserSaveStatus(it)
            }
        }
    }

    private fun handleUserSaveStatus(result: Result<Boolean>) {
        when (result) {
            is Result.Error -> setErrorState(result.exception)
            Result.Loading -> setLoadingState()
            is Result.Success -> setUserSignedState()
        }
    }


    private fun setErrorState(exception: Exception) {
        _loginUiState.update { currentLoginUiState ->
            val errorMessage =
                currentLoginUiState.error + exception.localizedMessage.orEmpty()
            currentLoginUiState.copy(error = errorMessage, loading = false)
        }
    }

    private fun setLoadingState() {
        _loginUiState.update { currentLoginUiState ->
            currentLoginUiState.copy(loading = true)
        }
    }

    private fun setUserSignedState() {
        _loginUiState.update { currentLoginUiState ->
            val isUserSigned = currentLoginUiState.isUSerSigned + true
            currentLoginUiState.copy(isUSerSigned = isUserSigned, loading = false)
        }
    }

    fun errorShown() {
        _loginUiState.update {
            it.copy(error = emptyList())
        }
    }

    fun navigatedHomePage() {
        _loginUiState.update {
            it.copy(isUSerSigned = emptyList())
        }
    }
}