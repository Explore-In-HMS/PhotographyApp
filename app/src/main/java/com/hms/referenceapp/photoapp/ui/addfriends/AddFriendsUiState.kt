package com.hms.referenceapp.photoapp.ui.addfriends

import com.hms.referenceapp.photoapp.data.model.PendingRequestUiModel
import com.hms.referenceapp.photoapp.data.model.UserProfileUiModel
import com.hms.referenceapp.photoapp.data.model.UserRelationship
import com.hms.referenceapp.photoapp.data.model.UserSelectUiModel

data class AddFriendsUiState(
    var loading: Boolean,
    val savedUserList: List<UserSelectUiModel>,
    val isUserRelationSaved: Boolean,
    var pendingRequestList: MutableList<PendingRequestUiModel>,
    val error: List<String>
) {
    companion object {
        fun initial() = AddFriendsUiState(
            loading = false,
            savedUserList= emptyList(),
            isUserRelationSaved = false,
            pendingRequestList = mutableListOf(),
            error = emptyList()
        )
    }
}