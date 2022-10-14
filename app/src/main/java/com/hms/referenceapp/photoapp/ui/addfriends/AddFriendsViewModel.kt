package com.hms.referenceapp.photoapp.ui.addfriends

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.hms.referenceapp.photoapp.common.Result
import com.hms.referenceapp.photoapp.data.model.*
import com.hms.referenceapp.photoapp.data.repository.AuthenticationRepository
import com.hms.referenceapp.photoapp.data.repository.CloudDbRepository
import com.hms.referenceapp.photoapp.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddFriendsViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val cloudDbRepository: CloudDbRepository
) : BaseViewModel() {
    private val _addFriendsUiState = MutableStateFlow(AddFriendsUiState.initial())
    val addFriendsUiState: StateFlow<AddFriendsUiState> get() = _addFriendsUiState.asStateFlow()

    lateinit var userId : String

    fun getUsers() {
        cloudDbRepository.getUsers()
        viewModelScope.launch {
            cloudDbRepository.cloudDbUserResponse.collect {
                if (it != null) {
                    handleGetUserListStatus(it)
                }
            }
        }
    }

    fun getPendingRequests(currentUserId: String){
        userId = currentUserId
        cloudDbRepository.getPendingRequests()
        viewModelScope.launch {
            cloudDbRepository.cloudDbUserRelationResponse.collect{ userRelationList ->
                if (userRelationList != null) {
                    val currentRequestList = mutableListOf<PendingRequestUiModel>()
                    addFriendsUiState.value.pendingRequestList.clear()
                    //_addFriendsUiState.value.userRelationList = userRelationList
                    userRelationList.forEach {
                        if (it.secondUserId == currentUserId){
                            if ( it.pendingFirstSecond && !it.areFriends){
                                val pendingRequestUiModel = PendingRequestUiModel(
                                    it.firstUserId,
                                    getUserNameFromId(it.firstUserId),
                                    isAccepted = false,
                                    isDeclined = false,
                                )
                                currentRequestList.add(pendingRequestUiModel)
                                addFriendsUiState.value.pendingRequestList.add(pendingRequestUiModel)
                            }
                        }
                    }
                    _addFriendsUiState.update { currentRequestListUiState ->
                        currentRequestListUiState.copy(
                            pendingRequestList = currentRequestList
                        )
                    }
                }
            }
        }
    }

    fun getFilteredList(username: String): List<UserSelectUiModel>{
        val filteredList =  mutableListOf<UserSelectUiModel>()
        _addFriendsUiState.value.savedUserList.forEach { userModel ->
            if (userModel.user.name.contains(username)){
                filteredList.add(userModel)
            }
        }
        return filteredList
    }

    private fun getUserNameFromId(id: String): String{
        _addFriendsUiState.value.savedUserList.forEach { userModel ->
            if (userModel.user.id.toString() == id){
                return userModel.user.name
            }
        }
        return "" //should be updated
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun updatePendingRequest(pendingRequest: PendingRequestUiModel) {
        val currentRequestList = _addFriendsUiState.value.pendingRequestList
        currentRequestList.find {
            it.id == pendingRequest.id
        }?.also {
            it.isAccepted = pendingRequest.isAccepted
            it.isDeclined = pendingRequest.isDeclined

            if (it.isAccepted == true) {
                sendFriendRequestResponse(it.id.toString(), userId, true)
            }

            if (it.isDeclined == true){
                sendFriendRequestResponse(it.id.toString(), userId, false)
            }

            currentRequestList.remove(it)
        }
        _addFriendsUiState.update { currentRequestListUiState ->
            currentRequestListUiState.copy(
               pendingRequestList = currentRequestList
            )
        }
    }


    private fun handleGetUserListStatus(result: List<User>?) {
        val mutableResultList = result?.toMutableList()
        mutableResultList?.forEach {
            if (it.id.toString() == userId){
                mutableResultList.remove(it)
            }
        }
        mutableResultList?.let {
            val userUiModelList = mutableResultList.map {
                it.toUserSelectUiModel()
            }
            _addFriendsUiState.update { currentUserListUiState ->
                currentUserListUiState.copy(
                    savedUserList = userUiModelList
                )
            }
        }
    }
    private fun User.toUserSelectUiModel(): UserSelectUiModel {
        return UserSelectUiModel(this, false)
    }

    @ExperimentalCoroutinesApi
    fun sendFriendRequest(
        firstUId: String,
        secondUId: String
    ) {
        val userRelationship = UserRelationship().apply {
            firstUserId = firstUId
            secondUserId = secondUId
            firstSecondUID = firstUId + secondUId
            pendingFirstSecond = true
            areFriends = false
        }

        viewModelScope.launch {
            authenticationRepository.saveUserRelationshipToCloud(userRelationship).collect {
                handleUserRelationSaveStatus(it)
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun sendFriendRequestResponse(
        firstUId: String,
        secondUId: String,
        addAsFriend: Boolean
    ) {
        val userRelationship = UserRelationship().apply {
            firstUserId = firstUId
            secondUserId = secondUId
            firstSecondUID = firstUId + secondUId
            pendingFirstSecond = false
            areFriends = addAsFriend
        }

        viewModelScope.launch {
            authenticationRepository.saveUserRelationshipToCloud(userRelationship).collect {
                handleUserRelationSaveStatus(it)
            }
        }
    }

    private fun handleUserRelationSaveStatus(result: Result<Boolean>) {
        when (result) {
            is Result.Error -> setErrorState(result.exception)
            Result.Loading -> setLoadingState()
            is Result.Success -> setUserRelationSavedState()
        }
    }

    private fun setErrorState(exception: Exception) {
        _addFriendsUiState.update { currentProfileUiState ->
            val errorMessage =
                currentProfileUiState.error + exception.localizedMessage.orEmpty()
            currentProfileUiState.copy(error = errorMessage, loading = false)
        }
    }

    private fun setLoadingState() {
        _addFriendsUiState.update { currentAddFriendsUiState ->
            currentAddFriendsUiState.copy(loading = true)
        }
    }

    private fun setUserRelationSavedState() {
        _addFriendsUiState.update { currentAddFriendsUiState ->
            val isUserRelationshipSaved = true
            currentAddFriendsUiState.copy(isUserRelationSaved = isUserRelationshipSaved, loading = false)
        }
    }

}