/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.shareimagedetail

import android.graphics.Bitmap
import com.hms.referenceapp.photoapp.data.model.Photos

data class SharePhotoUiState(
    val id: Int,
    val fileId: String,
    val title: String,
    val description: String,
    val sharedPersonCount: String,
    val photos: List<Bitmap>,
    val updatedPhotos: List<Photos>,
    val loading: Boolean,
    val error: String?,
    val isPhotosSharedSuccessuflly: Boolean,
    var arePhotosDuplicate: Boolean
) {
    companion object {
        fun initial() = SharePhotoUiState(
            id = -1,
            fileId = "",
            title = "",
            description = "",
            sharedPersonCount = "",
            photos = emptyList(),
            updatedPhotos = emptyList(),
            loading = false,
            error = null,
            isPhotosSharedSuccessuflly = false,
            arePhotosDuplicate = false
        )
    }
}
