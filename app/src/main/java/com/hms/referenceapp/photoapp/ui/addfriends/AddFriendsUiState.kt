package com.hms.referenceapp.photoapp.ui.addfriends

import com.hms.referenceapp.photoapp.data.model.UserProfileUiModel
import com.hms.referenceapp.photoapp.data.model.UserRelationship
import com.hms.referenceapp.photoapp.data.model.UserSelectUiModel

data class AddFriendsUiState(
    val loading: Boolean,
    val savedUserList: List<UserSelectUiModel>,
    val isUserRelationSaved: Boolean,
    var userRelationList: MutableList<UserRelationship>,
    val error: List<String>
) {
    companion object {
        fun initial() = AddFriendsUiState(
            loading = false,
            savedUserList= emptyList(),
            isUserRelationSaved = false,
            userRelationList = mutableListOf(),
            error = emptyList()
        )
    }
}