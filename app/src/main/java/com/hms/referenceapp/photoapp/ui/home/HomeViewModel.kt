/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.home


import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.viewModelScope
import com.hms.referenceapp.photoapp.data.model.ClassificationModel
import com.hms.referenceapp.photoapp.data.model.PhotoModel
import com.hms.referenceapp.photoapp.data.model.mlkit.ClassificationParameter
import com.hms.referenceapp.photoapp.data.repository.ImageClassificationRepository
import com.hms.referenceapp.photoapp.ui.base.BaseViewModel
import com.hms.referenceapp.photoapp.util.GeoDegreeConverter
import com.hms.referenceapp.photoapp.util.ext.toBitmap
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.mlsdk.classification.MLImageClassificationAnalyzer
import com.huawei.hms.mlsdk.common.MLFrame
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.stream.Collectors
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val imageClassificationRepository: ImageClassificationRepository,
    private val mlKitInstance: MLImageClassificationAnalyzer,
    private val agConnectUser: AGConnectAuth
) : BaseViewModel() {

    private var allPhotoModelsList: MutableList<PhotoModel> = mutableListOf()
    private var dbResult: List<ClassificationModel> = listOf()

    private var filteredList: Map<String, Int> = mapOf()
    private var filteredListMostCounted: Map<String, Int> = mapOf()

    private var mlKitResponseMutableList: MutableList<List<ClassificationModel>> = mutableListOf()

    private var dataInitial: List<ClassificationModel> = mutableListOf()
    private val _data = MutableStateFlow(dataInitial)
    val data: StateFlow<List<ClassificationModel>> get() = _data.asStateFlow()

    private val _photoUiState = MutableStateFlow(PhotoUiState.initial())
    val photoUiState: StateFlow<PhotoUiState> get() = _photoUiState.asStateFlow()

    fun isUserLogged() = agConnectUser.currentUser != null

    fun getAllImagesFromSdCard(contentResolver: ContentResolver) {

        viewModelScope.launch(Dispatchers.IO) {
            val allPhotoModels: MutableList<PhotoModel> = mutableListOf()
            val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
            )
            val orderBy: String = MediaStore.Images.Media.DATE_ADDED
            val cursor = contentResolver.query(uri, projection, null, null, "$orderBy DESC")!!
            val columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (cursor.moveToNext()) {
                var photoUri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    cursor.getString(columnIndexData)
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    photoUri = MediaStore.setRequireOriginal(photoUri)
                }

                val locationInfo = getLocationInfoFromPhoto(contentResolver, photoUri)

                val absolutePathOfImage =
                    ContentUris.withAppendedId(uri, cursor.getLong(columnIndexData)).toString()
                allPhotoModels.add(
                    PhotoModel(
                        path = absolutePathOfImage,
                        coordinate = locationInfo
                    )
                )
            }

            allPhotoModelsList = allPhotoModels

            withContext(Dispatchers.Main) {
                _photoUiState.value = PhotoUiState(
                    allPhotoModels = allPhotoModels
                )
            }
            cursor.close()
        }
    }


    private fun getLocationInfoFromPhoto(contentResolver: ContentResolver, photoUri: Uri): LatLng? {
        return try {
            contentResolver.openInputStream(photoUri).use { inputStream ->
                val exif = inputStream?.let { ExifInterface(it) }
                GeoDegreeConverter(exif).latLng
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getMapStartingLocation(): LatLng? {
        val numberOfLocations =
            photoUiState.value.getPhotosWhichHasLocation()
                .groupingBy { it.coordinate }
                .eachCount()
        return if (numberOfLocations.isEmpty()) null else numberOfLocations.toList()
            .sortedByDescending { it.second }[0].first
    }

    fun getPhotoUiState() = photoUiState.value

    // ML Kit Implementation

    fun getThemeTagFromImage(
        contentResolver: ContentResolver,
        path: List<PhotoModel>,
        requestFlag: Boolean,
        photoSize: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (requestFlag) {
                getTagsFromMlKit(contentResolver, path,photoSize)
            } else {
                getTagsFromDB(photoSize)
            }
        }
    }


    private fun getTagsFromMlKit(contentResolver: ContentResolver, path: List<PhotoModel>, photoSize: Int) {
        val classification: MutableList<ClassificationModel> = mutableListOf()
        mlKitResponseMutableList.clear()
        for (pathItem in path) {
            val classificationParameter: MutableList<ClassificationParameter> =
                mutableListOf()
            val frame = // please create MLFrame object here

            // ML Kit -  Image Classification Async Process
            val task = // please add necessary function here
            task.addOnSuccessListener {
                it.forEach { imageClassification ->
                    classificationParameter.add(
                        ClassificationParameter(
                            imageClassification.name,
                            imageClassification.possibility
                        )
                    )
                }
                classification.add(
                    ClassificationModel(
                        imagePath = pathItem.path,
                        resultParameters = classificationParameter
                    )
                )

                if (classification.isNotEmpty() && classification.size == photoSize) {
                    _data.value = classification
                    viewModelScope.launch(Dispatchers.IO) {
                        insertDB(dataList = classification)
                    }
                }
            }
        }
    }

    private fun getTagsFromDB(photoSize: Int) {
        dbResult = imageClassificationRepository.getAllClassification()
        if (dbResult.isNotEmpty() && dbResult.size == photoSize) {
            _data.value = dbResult
        }
    }

    private suspend fun insertDB(dataList: List<ClassificationModel>) {
        // Room DB Delete
        imageClassificationRepository.deleteAllClassification()
        // Room Insert
        val roomResultList: MutableList<Long> = mutableListOf()
        for (listItem in dataList) {
            val result = imageClassificationRepository.insertClassification(
                ClassificationModel(
                    imagePath = listItem.imagePath,
                    resultParameters = listItem.resultParameters
                )
            )
            roomResultList.add(result)
        }
        Log.i("RoomResult", roomResultList.toString())
    }

    // Filter And Sort Processes
    fun filterAndSortParameterList(parameterList: List<String>): Map<String, Int> {
        // Filter by tag count
        filteredList = parameterList.groupingBy {
            it
        }.eachCount().filter { tagCount ->
            tagCount.value >= 1
        }

        // Sort Tag Counts
        filteredList =
            filteredList.toList().sortedByDescending { (_, value) -> value }.toMap()

        // Get first 10 item and sort again
        filteredListMostCounted = filteredList.entries.stream()
            .limit(10)
            .collect(Collectors.toMap({ (key) -> key }) { (_, value) -> value }).toList()
            .sortedByDescending { (_, value) -> value }.toMap()

        return filteredListMostCounted
    }

    fun setAllPhotosCount(context: Context, allPhotosCount: Int) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("PhotoCount", MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putInt("AllPhotosCount", allPhotosCount)
        editor.apply()
    }

    fun getAllPhotosCount(context: Context): Int {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("PhotoCount", MODE_PRIVATE)

        return when (val result = sharedPreferences.getInt("AllPhotosCount", MODE_PRIVATE)) {
            0 -> 0
            else -> result
        }
    }
}
