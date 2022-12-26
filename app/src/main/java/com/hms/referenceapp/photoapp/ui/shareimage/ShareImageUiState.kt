/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.shareimage

import com.hms.referenceapp.photoapp.data.model.PhotoDetails

data class ShareImageUiState(
    val loading: Boolean,
    val isSavedFilesWithPerson: List<Boolean>,
    val filesYouSharedList: List<PhotoDetails>,
    val sharedFilesWithYouList: List<PhotoDetails>,
    val sharedFilesWithYouReceiverList: List<PhotoDetails>,
    val error: List<String>
) {
    companion object {
        fun initial() = ShareImageUiState(
            loading = false,
            isSavedFilesWithPerson = emptyList(),
            filesYouSharedList = emptyList(),
            sharedFilesWithYouList = emptyList(),
            sharedFilesWithYouReceiverList = emptyList(),
            error = emptyList()
        )
    }
}