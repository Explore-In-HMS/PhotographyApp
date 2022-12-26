/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.data.repository

import android.util.Log
import com.hms.referenceapp.photoapp.R
import com.hms.referenceapp.photoapp.common.Result
import com.hms.referenceapp.photoapp.data.model.PhotoDetails
import com.hms.referenceapp.photoapp.data.model.Photos
import com.hms.referenceapp.photoapp.data.model.User
import com.hms.referenceapp.photoapp.data.model.UserRelationship
import com.hms.referenceapp.photoapp.data.remote.ObjectTypeInfoHelper
import com.hms.referenceapp.photoapp.di.ResourceProvider
import com.hms.referenceapp.photoapp.util.Event
import com.huawei.agconnect.cloud.database.*
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudDbRepository @Inject constructor(
    private val cloudDB: AGConnectCloudDB,
    private val resourceProvider: ResourceProvider
) {

    private var cloudDBZone: CloudDBZone? = null
    private var mRegisterFilesYouShared: ListenerHandler? = null
    private var mRegisterSharedFilesWithYou: ListenerHandler? = null
    private var mRegisterSharedFilesWithYouReceivers: ListenerHandler? = null

    private val initialFilesYouSharedResponseList: MutableList<PhotoDetails>? = null
    private val _filesYouSharedResponse = MutableStateFlow(initialFilesYouSharedResponseList)
    val filesYouSharedResponse: StateFlow<MutableList<PhotoDetails>?> get() = _filesYouSharedResponse.asStateFlow()

    private val initialSharedFilesWithYouResponseList: MutableList<PhotoDetails>? = null
    private val _sharedFilesWithYouResponse =
        MutableStateFlow(initialSharedFilesWithYouResponseList)
    val sharedFilesWithYouResponse: StateFlow<MutableList<PhotoDetails>?> get() = _sharedFilesWithYouResponse.asStateFlow()

    private val initialSharedFilesWithYouReceiverResponseList: MutableList<PhotoDetails>? = null
    private val _sharedFilesWithYouReceiverResponse =
        MutableStateFlow(initialSharedFilesWithYouReceiverResponseList)
    val sharedFilesWithYouReceiverResponse: StateFlow<MutableList<PhotoDetails>?> get() = _sharedFilesWithYouReceiverResponse.asStateFlow()

    private val initialCloudDbZone: CloudDBZone? = null
    private val _cloudDbZoneFlow = MutableStateFlow(initialCloudDbZone)
    val cloudDbZoneFlow: StateFlow<CloudDBZone?> get() = _cloudDbZoneFlow.asStateFlow()

    private val initialCloudDbUserResponseList: MutableList<User>? = null
    private val _cloudDbUserResponse = MutableStateFlow(initialCloudDbUserResponseList)
    val cloudDbUserResponse: StateFlow<MutableList<User>?> get() = _cloudDbUserResponse.asStateFlow()

    private val initialCloudDbUserRelationResponseList: MutableList<UserRelationship>? = null
    private val _cloudDbUserRelationResponse = MutableStateFlow(initialCloudDbUserRelationResponseList)
    val cloudDbUserRelationResponse: StateFlow<MutableList<UserRelationship>?> get() = _cloudDbUserRelationResponse.asStateFlow()

    private var limit = 0
    private var remind = 0

    private val allSharedPhotos: MutableList<Photos> = ArrayList()

    private val _allSharedPhotosResponse =
        MutableStateFlow<Event<Result<List<Photos>>>>(Event(Result.Success(emptyList())))

    val allSharedPhotosResponse get() = _allSharedPhotosResponse.asStateFlow()

    private val _deleteSharedPhotosResponse =
        MutableStateFlow<Event<Result<Photos>>>(Event(Result.Success(Photos())))
    val deleteSharedPhotosResponse get() = _deleteSharedPhotosResponse.asStateFlow()

    private val _deleteUserResponse =
        MutableStateFlow<Event<Result<PhotoDetails>>>(Event(Result.Success(PhotoDetails())))

    val deleteUserResponse get() = _deleteUserResponse.asStateFlow()

    init {
        openDb()
    }

    private fun openDb() {
        if (cloudDBZone == null) {
            val mConfig = CloudDBZoneConfig(
                DB_NAME,
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC
            )
            createObjectType()
            mConfig.persistenceEnabled = true
            cloudDB.openCloudDBZone2(mConfig, true)
                .addOnSuccessListener {
                    Log.i("CloudDB", "Open cloudDBZone success")
                    cloudDBZone = it
                    _cloudDbZoneFlow.value = it
                    getUsers()
                }
                .addOnFailureListener {
                    Log.w("CloudDB", "Open cloudDBZone failed for " + it.message)
                }
        } else {
            Log.e("Cloud DB", "error")
        }
    }

    private fun createObjectType() {
        cloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo())
    }

    fun saveToCloudDB(cloudDBZoneObject: CloudDBZoneObject): Flow<Result<Boolean>> = callbackFlow {
        trySend(Result.Loading)
        if (isDpOpen().not()) {
            trySend(Result.Error(Exception(resourceProvider.getString(R.string.exp_sht_went_wrong))))
            close()
            return@callbackFlow
        }

        val upsertTask = // Define the upsertTask properly here
        upsertTask.addOnSuccessListener {
            trySend(Result.Success(true))
        }.addOnFailureListener { exception ->
            trySend(Result.Error(exception))
        }.addOnCompleteListener {
            close()
        }
        awaitClose {
            upsertTask.addOnSuccessListener(null)
            upsertTask.addOnFailureListener(null)
            upsertTask.addOnCompleteListener(null)
        }

    }.flowOn(Dispatchers.IO)

    private fun isDpOpen(): Boolean = cloudDBZone != null

    // Updating Listening in Real Time
    private val mSnapshotListenerForFilesYouShared =
        OnSnapshotListener<PhotoDetails> { cloudDBZoneSnapshot, e ->
            if (e != null) {
                Log.w(TAG, "onSnapshot: " + e.message)
                return@OnSnapshotListener
            }
            val snapshotObjects = cloudDBZoneSnapshot.snapshotObjects
            val filesYouSharedList: MutableList<PhotoDetails> = ArrayList()
            try {
                if (snapshotObjects != null) {
                    while (snapshotObjects.hasNext()) {
                        val fileData = snapshotObjects.next()
                        filesYouSharedList.add(fileData)
                    }
                }

                _filesYouSharedResponse.value = filesYouSharedList

            } catch (snapshotException: AGConnectCloudDBException) {
                Log.w(TAG, "onSnapshot:(getObject) " + snapshotException.message)
            } finally {
                cloudDBZoneSnapshot.release()
            }
        }

    fun addSubscriptionForFilesYouShared(
        cloudDBZoneInstance: CloudDBZone,
        queryTitle: String,
        queryValue: String
    ) {
        try {
            if (isDpOpen().not()) {
                return
            }
            val snapshotQuery = CloudDBZoneQuery.where(PhotoDetails::class.java)
                .equalTo(queryTitle, queryValue)
            mRegisterFilesYouShared = cloudDBZoneInstance.subscribeSnapshot(
                snapshotQuery,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY,
                mSnapshotListenerForFilesYouShared
            )
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, "subscribeSnapshot: " + e.message)
        }
    }

    private val mSnapshotListenerForSharedFilesWithYou =
        OnSnapshotListener<PhotoDetails> { cloudDBZoneSnapshot, e ->
            if (e != null) {
                Log.w(TAG, "onSnapshot: " + e.message)
                return@OnSnapshotListener
            }
            val snapshotObjects = cloudDBZoneSnapshot.snapshotObjects
            val sharedFilesWithYouList: MutableList<PhotoDetails> = ArrayList()
            try {
                if (snapshotObjects != null) {
                    while (snapshotObjects.hasNext()) {
                        val fileData = snapshotObjects.next()
                        sharedFilesWithYouList.add(fileData)
                    }
                }

                _sharedFilesWithYouResponse.value = sharedFilesWithYouList

            } catch (snapshotException: AGConnectCloudDBException) {
                Log.w(TAG, "onSnapshot:(getObject) " + snapshotException.message)
            } finally {
                cloudDBZoneSnapshot.release()
            }
        }

    fun addSubscriptionForSharedFilesWithYou(
        cloudDBZoneInstance: CloudDBZone,
        queryTitle: String,
        queryValue: String
    ) {
        try {
            if (isDpOpen().not()) {
                return
            }
            val snapshotQuery = // Add the proper query function here
            mRegisterSharedFilesWithYou = cloudDBZoneInstance.subscribeSnapshot(
                snapshotQuery,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY,
                mSnapshotListenerForSharedFilesWithYou
            )
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, "subscribeSnapshot: " + e.message)
        }
    }


    private val mSnapshotListenerForSharedFilesWithYouReceivers =
        OnSnapshotListener<PhotoDetails> { cloudDBZoneSnapshot, e ->
            if (e != null) {
                Log.w(TAG, "onSnapshot: " + e.message)
                return@OnSnapshotListener
            }
            val snapshotObjects = cloudDBZoneSnapshot.snapshotObjects
            val sharedFilesWithYouReceiverList: MutableList<PhotoDetails> = ArrayList()
            try {
                if (snapshotObjects != null) {
                    while (snapshotObjects.hasNext()) {
                        val fileData = snapshotObjects.next()
                        sharedFilesWithYouReceiverList.add(fileData)
                    }
                }

                _sharedFilesWithYouReceiverResponse.value = sharedFilesWithYouReceiverList

            } catch (snapshotException: AGConnectCloudDBException) {
                Log.w(TAG, "onSnapshot:(getObject) " + snapshotException.message)
            } finally {
                cloudDBZoneSnapshot.release()
            }
        }

    fun addSubscriptionForSharedFilesWithYouReceivers(
        cloudDBZoneInstance: CloudDBZone,
        queryTitle: String,
        queryValue: String
    ) {
        try {
            if (isDpOpen().not()) {
                return
            }
            val snapshotQuery = CloudDBZoneQuery.where(PhotoDetails::class.java)
                .equalTo(queryTitle, queryValue)
            mRegisterSharedFilesWithYouReceivers = cloudDBZoneInstance.subscribeSnapshot(
                snapshotQuery,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY,
                mSnapshotListenerForSharedFilesWithYouReceivers
            )
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, "subscribeSnapshot: " + e.message)
        }
    }

    fun unRegisterToListen() {
        try {
            mRegisterFilesYouShared?.remove()
            mRegisterSharedFilesWithYou?.remove()
            mRegisterSharedFilesWithYouReceivers?.remove()
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, "unRegister: " + e.message)
        }
    }

    fun getUsers() {
        if (cloudDBZone == null) {
            Log.w(TAG, resourceProvider.getString(R.string.exp_null_cloud_db_zone))
            return
        }
        val queryTask = // Define the queryTask properly here
        queryTask.addOnSuccessListener { snapshot -> processUserQueryResult(snapshot) }
            .addOnFailureListener {
            }
    }

    private fun processUserQueryResult(snapshot: CloudDBZoneSnapshot<User>) {
        val userInfoCursor = snapshot.snapshotObjects
        val userInfoList: MutableList<User> = ArrayList()
        try {
            while (userInfoCursor.hasNext()) {
                val userInfo = userInfoCursor.next()
                userInfoList.add(userInfo)
            }
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, "processQueryResult: " + e.message)
        } finally {
            snapshot.release()
        }
        _cloudDbUserResponse.value = userInfoList
    }

    private fun processUserRelationQueryResult(snapshot: CloudDBZoneSnapshot<UserRelationship>) {
        val userRelationInfoCursor = snapshot.snapshotObjects
        val userRelationInfoList: MutableList<UserRelationship> = ArrayList()
        try {
            while (userRelationInfoCursor.hasNext()) {
                val userRelationInfo = userRelationInfoCursor.next()
                userRelationInfoList.add(userRelationInfo)
            }
        } catch (e: AGConnectCloudDBException) {

        } finally {
            snapshot.release()
        }
        _cloudDbUserRelationResponse.value = userRelationInfoList
    }

    fun getSharedPhotos(fileId: String) {
        _allSharedPhotosResponse.value = Event(Result.Loading)
        if (cloudDBZone == null) {
            _allSharedPhotosResponse.value = Event(Result.Error(Exception(resourceProvider.getString(R.string.exp_sht_went_wrong))))
            return
        }
        cloudDBZone!!.executeCountQuery(
            CloudDBZoneQuery.where(Photos::class.java).equalTo(FILE_ID, fileId), ID,
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        ).addOnSuccessListener { count ->
            val firstLimit = getLimit(count.toInt())
            getPhotosWithPagination(
                fileId = fileId,
                lastPhotos = null,
                limit = firstLimit,
                onSuccessListener = {
                    val photos = getPhotosBySnapshot(it)
                    allSharedPhotos.addAll(photos)
                    _allSharedPhotosResponse.value = Event(Result.Success(photos))
                    remind = (count - firstLimit).toInt()
                    getMorePhotos(fileId)
                },
                onErrorListener = {
                    _allSharedPhotosResponse.value = Event(Result.Error(it))
                })
        }.addOnFailureListener {
            _allSharedPhotosResponse.value = Event(Result.Error(it))
        }
    }

    fun deleteSharedFile(fileId: Int) {
        val fileToDelete = PhotoDetails()

        fileToDelete.id = fileId
        if (cloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it")
            return
        }

        val deleteTask = cloudDBZone!!.executeDelete(fileToDelete)
        deleteTask.addOnSuccessListener {
        }.addOnFailureListener { }
    }


    private fun getMorePhotos(
        fileId: String
    ) {
        if (remind > 0) {
            limit = getLimit(remind)
            getPhotosWithPagination(fileId = fileId,
                lastPhotos = allSharedPhotos.last(),
                limit = limit,
                onSuccessListener = {
                    val photos = getPhotosBySnapshot(it)
                    allSharedPhotos.addAll(photos)
                    _allSharedPhotosResponse.value = Event(Result.Success(photos))
                    remind -= limit
                    getMorePhotos(fileId)
                }, onErrorListener = {
                    _allSharedPhotosResponse.value = Event(Result.Error(it))
                })
        }
    }


    private fun getPhotosWithPagination(
        fileId: String,
        lastPhotos: Photos?,
        limit: Int,
        onSuccessListener: (snapshot: CloudDBZoneSnapshot<Photos>) -> Unit,
        onErrorListener: (Exception) -> Unit
    ) {
        val query = CloudDBZoneQuery.where(Photos::class.java)
            .equalTo("fileId", fileId)
            .orderByAsc("id")
            .also { query ->
                lastPhotos?.let {
                    query.startAfter(it)
                }
            }.limit(limit)

        cloudDBZone!!.executeQuery(
            query,
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        ).addOnSuccessListener { snapshot ->
            onSuccessListener.invoke(snapshot)
        }.addOnFailureListener {
            onErrorListener.invoke(it)
        }
    }

    private fun getPhotosBySnapshot(snapshot: CloudDBZoneSnapshot<Photos>): List<Photos> {
        val photosCursor = snapshot.snapshotObjects
        val sharedPhotos: MutableList<Photos> = ArrayList()
        return try {
            while (photosCursor.hasNext()) {
                val photo = photosCursor.next()
                sharedPhotos.add(photo)
            }
            sharedPhotos
        } catch (e: AGConnectCloudDBException) {
            emptyList()
        } finally {
            snapshot.release()
        }
    }


    fun getPendingRequests() {
        if (cloudDBZone == null) {
            return
        }
        val queryTask = // Define the queryTask properly here
        queryTask.addOnSuccessListener { snapshot ->
            processUserRelationQueryResult(snapshot)
        }
            .addOnFailureListener {

            }
    }


    fun deletePhotos(id: Int) {
        _deleteSharedPhotosResponse.value = Event(Result.Loading)
        if (cloudDBZone == null) {
            _deleteSharedPhotosResponse.value = Event(Result.Error(Exception(resourceProvider.getString(R.string.exp_sht_went_wrong))))
            return
        }

        val query: CloudDBZoneQuery<Photos> = CloudDBZoneQuery.where(Photos::class.java).equalTo(ID, id)
        val queryTask = cloudDBZone!!.executeQuery(
            query,
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        )

        queryTask.addOnSuccessListener { snapshot ->
            var photo = Photos()
            while (snapshot.snapshotObjects.hasNext()) {
                photo = snapshot.snapshotObjects.next()
            }
            val deleteTask = cloudDBZone!!.executeDelete(photo)
            deleteTask.addOnSuccessListener {
                _deleteSharedPhotosResponse.value = Event(Result.Success(photo))
            }
            deleteTask.addOnFailureListener {
                _deleteSharedPhotosResponse.value =
                    Event(Result.Error(Exception(resourceProvider.getString(R.string.exp_sht_went_wrong_deleting))))
            }
        }.addOnFailureListener {
            _deleteSharedPhotosResponse.value =
                Event(Result.Error(Exception(resourceProvider.getString(R.string.exp_sht_went_wrong))))
        }
    }

    private fun getLimit(count: Int) = (if (count >= 4) 4 else count)

    fun deleteUserFromSharedFile(_fileId: String, _receiverId: Long) {
        _deleteUserResponse.value = Event(Result.Loading)
        if (cloudDBZone == null) {
            _deleteUserResponse.value = Event(Result.Error(Exception(resourceProvider.getString(R.string.exp_sht_went_wrong))))
            return
        }

        val query: CloudDBZoneQuery<PhotoDetails> =
            CloudDBZoneQuery.where(PhotoDetails::class.java).equalTo(FILE_ID, _fileId)
        val queryTask = cloudDBZone!!.executeQuery(
            query,
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        )

        val updatedList = arrayListOf<PhotoDetails>()

        queryTask.addOnSuccessListener { snapshot ->
            var photoDetailDeleted = PhotoDetails()
            while (snapshot.snapshotObjects.hasNext()) {
                val photoDetail = snapshot.snapshotObjects.next()
                if (photoDetail.receiverId.equals(_receiverId.toString())) {
                    photoDetailDeleted = photoDetail
                }
                else {
                    updatedList.add(
                        PhotoDetails().apply {
                            id = photoDetail.id
                            senderId = photoDetail.senderId
                            senderName = photoDetail.senderName
                            receiverId = photoDetail.receiverId
                            receiverName = photoDetail.receiverName
                            fileId = photoDetail.fileId
                            fileName = photoDetail.fileName
                            fileDesc = photoDetail.fileDesc
                            numberOfPeopleShared = (photoDetail.numberOfPeopleShared.toInt() - 1).toString()
                        }
                    )
                }
            }
            val deleteTask = cloudDBZone!!.executeDelete(photoDetailDeleted)
            deleteTask.addOnSuccessListener {
                _deleteUserResponse.value = Event(Result.Success(photoDetailDeleted))
                updateSharedPeopleCount(updatedList)
            }
            deleteTask.addOnFailureListener { exception ->
                _deleteUserResponse.value = Event(Result.Error(exception))
            }
        }.addOnFailureListener { exception ->
            _deleteUserResponse.value = Event(Result.Error(exception))
        }
    }

    private fun updateSharedPeopleCount(photoDetailList: List<PhotoDetails>){
        if (cloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it")
        } else{
            val upsertTask = cloudDBZone!!.executeUpsert(photoDetailList)
            upsertTask.addOnSuccessListener { cloudDBZoneResult ->
                Log.e(TAG, cloudDBZoneResult.toString())
            }.addOnFailureListener {
                Log.e(TAG, it.message.toString())
            }
        }
    }

    companion object {
        private const val DB_NAME = "PhotoAppDB"
        private const val TAG = "CloudDB"
        private const val FILE_ID = "fileId"
        private const val ID = "id"
    }
}