package com.hms.referenceapp.photoapp.ui.profile

import com.hms.referenceapp.photoapp.data.model.UserProfileUiModel
import com.hms.referenceapp.photoapp.data.model.UserRelationship
import com.hms.referenceapp.photoapp.data.model.UserSelectUiModel

data class ProfileUiState(
    val loading: Boolean,
    val isUserSignedOut: List<Boolean>,
    val userProfile: UserProfileUiModel,
    val error: List<String>
) {
    companion object {
        fun initial() = ProfileUiState(
            loading = false,
            isUserSignedOut = emptyList(),
            userProfile = UserProfileUiModel(null, null,null),
            error = emptyList()
        )
    }
}