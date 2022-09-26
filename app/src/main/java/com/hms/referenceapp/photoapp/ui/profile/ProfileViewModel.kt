package com.hms.referenceapp.photoapp.ui.profile

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.hms.referenceapp.photoapp.data.model.UserProfileUiModel
import com.hms.referenceapp.photoapp.data.repository.AuthenticationRepository
import com.hms.referenceapp.photoapp.ui.base.BaseViewModel
import com.huawei.agconnect.auth.AGConnectAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

        val userProfileUiModel = UserProfileUiModel(userName, userImageFromCurrentUser)
        viewModelScope.launch {
            _profileUiState.update {
                it.copy(userProfile = userProfileUiModel)
            }
        }
    }
}