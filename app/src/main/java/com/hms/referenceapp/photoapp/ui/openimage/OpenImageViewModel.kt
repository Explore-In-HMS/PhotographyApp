/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.openimage

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.viewModelScope
import com.hms.referenceapp.photoapp.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class OpenImageViewModel @Inject constructor(
) : BaseViewModel() {

    private var savedImage: Boolean? = null
    private val initialSaveImageResult: Boolean? = null
    private val _saveImageResultFlow = MutableStateFlow(initialSaveImageResult)
    val saveImageResultFlow: StateFlow<Boolean?> get() = _saveImageResultFlow.asStateFlow()

    private var editedImageUri: Uri? = null
    private val initialEditedImageUri: Uri? = null
    private val _editedImageUriFlow = MutableStateFlow(initialEditedImageUri)
    val editedImageUriFlow: StateFlow<Uri?> get() = _editedImageUriFlow.asStateFlow()

    fun saveEditedPhoto(context: Context, bitmap: Bitmap) {
        viewModelScope.launch {
            val result = kotlin.runCatching {

                var fos: OutputStream? = null

                val timeStamp =
                    SimpleDateFormat("ddMMyyyy_HHmm", Locale.getDefault()).format(Date())
                val name = "MI_$timeStamp"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver: ContentResolver = context.contentResolver
                    val contentValues = ContentValues()
                    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name)
                    contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    contentValues.put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "DCIM/${OpenImageFragment.ALBUM_NAME}"
                    )
                    contentValues.put(
                        MediaStore.Images.Media.DATE_TAKEN,
                        System.currentTimeMillis()
                    )
                    contentValues.put(
                        MediaStore.Images.Media.DATE_ADDED,
                        System.currentTimeMillis() / 1000
                    )
                    editedImageUri =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    if (editedImageUri != null) {
                        fos = resolver.openOutputStream(editedImageUri!!)
                    }
                } else {

                    //For example, you want to save a downloaded pdf file to "Shared" Download directory.
                    //How do you do that ? For api 29 and above, you can do that without no permission.
                    //For api 28 and below, you need getExternalStoragePublicDirectory() method but it is deprecated.
                    // What if you don't want to use that deprecated method? Then you can use SAF file explorer(Intent#ACTION_OPEN_DOCUMENT).
                    // As said in the question, this requires the user to pick the location manually.

                    val imagesDir: String = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM
                    ).toString() + File.separator + name
                    val file = File(imagesDir)
                    if (!file.exists()) {
                        file.mkdir()
                    }
                    val image = File(imagesDir, "$name.png")
                    fos = FileOutputStream(image)
                }

                savedImage = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos?.flush()
                fos?.close()
            }

            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    _saveImageResultFlow.value = savedImage
                    _editedImageUriFlow.value = editedImageUri
                }
            }
        }
    }
}