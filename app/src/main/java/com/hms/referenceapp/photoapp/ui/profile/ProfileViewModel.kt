package com.hms.referenceapp.photoapp.ui.profile

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.hms.referenceapp.photoapp.common.Result
import com.hms.referenceapp.photoapp.data.model.User
import com.hms.referenceapp.photoapp.data.model.UserProfileUiModel
import com.hms.referenceapp.photoapp.data.model.UserRelationship
import com.hms.referenceapp.photoapp.data.model.UserSelectUiModel
import com.hms.referenceapp.photoapp.data.repository.AuthenticationRepository
import com.hms.referenceapp.photoapp.data.repository.CloudDbRepository
import com.hms.referenceapp.photoapp.ui.base.BaseViewModel
import com.hms.referenceapp.photoapp.ui.listuser.ListUserUiState
import com.huawei.agconnect.auth.AGConnectAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    agConnectUser: AGConnectAuth
) : BaseViewModel() {
    private val _profileUiState = MutableStateFlow(ProfileUiState.initial())
    val profileUiState: StateFlow<ProfileUiState> get() = _profileUiState.asStateFlow()

    init {
        showUserInformation(agConnectUser)
    }

    fun signOut() {
        viewModelScope.launch {
            authenticationRepository.signOut()
        }
    }

    private fun showUserInformation(agcUser: AGConnectAuth) {
        val userName = agcUser.currentUser.displayName
        val userImageFromCurrentUser = agcUser.currentUser.photoUrl
        val userId = agcUser.currentUser.uid

        val userProfileUiModel = UserProfileUiModel(userId,userName, userImageFromCurrentUser)
        viewModelScope.launch {
            _profileUiState.update {
                it.copy(userProfile = userProfileUiModel)
            }
        }
    }
}