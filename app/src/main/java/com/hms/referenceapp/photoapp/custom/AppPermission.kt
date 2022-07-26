/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.custom


import android.Manifest.permission.ACCESS_MEDIA_LOCATION
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.os.Build
import androidx.annotation.RequiresApi

sealed class AppPermission(
    val name: String
) {

    object ReadExternalStorage : AppPermission(READ_EXTERNAL_STORAGE)

    @RequiresApi(Build.VERSION_CODES.Q)
    object AccessMediaLocation : AppPermission(ACCESS_MEDIA_LOCATION)
}