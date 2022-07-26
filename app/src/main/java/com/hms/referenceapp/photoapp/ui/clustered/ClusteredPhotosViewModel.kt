/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.clustered

import android.content.ContentResolver
import android.net.Uri
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.hms.referenceapp.photoapp.adapter.ClusteredPhotosViewItem
import com.hms.referenceapp.photoapp.data.model.PhotoModel
import com.hms.referenceapp.photoapp.ui.base.BaseViewModel
import com.hms.referenceapp.photoapp.util.GeoDegreeConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class ClusteredPhotosViewModel @Inject constructor(
) : BaseViewModel() {

    val items = mutableListOf<ClusteredPhotosViewItem>()

    fun setClusteredItems(photoList: Array<PhotoModel>, contentResolver: ContentResolver) {

        photoList.groupBy {
            val date = getDateInfoFromPhoto(contentResolver, it.path.toUri())
            date?.getFormattedDate("yyyy:MM:dd HH:mm:ss")
        }.toList().sortedBy {(date, _) ->
            date
        }.forEach {(date,photos) ->
            items.add(ClusteredPhotosViewItem.Date(date?.toFormattedString()))
            photos.forEach { photo ->
                items.add(ClusteredPhotosViewItem.Image(photo.path))
            }
        }
    }

    private fun getDateInfoFromPhoto(contentResolver: ContentResolver, photoUri: Uri): String? {
        return try {
            contentResolver.openInputStream(photoUri).use { inputStream ->
                val exif = inputStream?.let { ExifInterface(it) }
                GeoDegreeConverter(exif).date
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun String.getFormattedDate(dateFormatPattern: String): LocalDate? {
        return try {
            val dateFormatter = DateTimeFormatter.ofPattern(dateFormatPattern)
            LocalDate.parse(this, dateFormatter)
        } catch (exception: DateTimeParseException) {
            null
        }
    }

    private fun LocalDate.toFormattedString(): String?{
        return try {
            this.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (exception: DateTimeParseException) {
            null
        }
    }
}