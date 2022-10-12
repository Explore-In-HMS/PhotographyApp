package com.hms.referenceapp.photoapp.ui.addfriends

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.hms.referenceapp.photoapp.common.Result
import com.hms.referenceapp.photoapp.data.model.User
import com.hms.referenceapp.photoapp.data.model.UserRelationship
import com.hms.referenceapp.photoapp.data.model.UserSelectUiModel
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

    fun getUsers() {
        cloudDbRepository.getUsers()
        viewModelScope.launch {
            cloudDbRepository.cloudDbUserResponse.collect {
                it
                println(it)
                if (it != null) {

                    handleGetUserListStatus(it)

                }else {

                }
                it
                //handleGetUserListStatus(it)
            }
        }
    }

    fun getPendingRequests(currentUserId: String){
        cloudDbRepository.getPendingRequests()
        viewModelScope.launch {
            cloudDbRepository.cloudDbUserRelationResponse.collect{ userRelationList ->
                userRelationList
                println(userRelationList)
                if (userRelationList != null) {
                    Log.d("pending","usersize ${userRelationList.size.toString()}")
                    _addFriendsUiState.value.userRelationList = userRelationList
                    userRelationList.forEach {
                        if (it.secondUserId == currentUserId){
                            if (it.pendingFirstSecond){
                                Log.d("pendingUserId",it.firstUserId)
                                //elimizde arkadaşlık istekleri kullanıcı bazlı olarak geliyor. Şimdi eğer
                                //kullanıcı isteği onaylarsa bu kişilerin arkadaş olması gerekiyor.
                                //bu durumda pending kalkıcak arefriends true olucak!
                                //daha sonra başka fragmentlardan arkadaş listesi çekilirken
                                //sadece areFriends true olanlar çekilecek!
                            }
                        }
                    }

                }else {
                    Log.d("pending","usersize null")
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

    private fun handleGetUserListStatus(result: List<User>?) {
        result?.let {
            val userUiModelList = result.map {
                it.toUserSelectUiModel()
            }

            _addFriendsUiState.update { currentUserListUiState ->
                Log.d("tafffUi",userUiModelList.size.toString())
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
    fun saveUserRelationToCloud(
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