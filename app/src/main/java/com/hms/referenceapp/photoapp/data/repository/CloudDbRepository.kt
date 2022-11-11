/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.data.repository

import android.util.Log
import com.hms.referenceapp.photoapp.common.Result
import com.hms.referenceapp.photoapp.data.model.PhotoDetails
import com.hms.referenceapp.photoapp.data.model.Photos
import com.hms.referenceapp.photoapp.data.model.User
import com.hms.referenceapp.photoapp.data.model.UserRelationship
import com.hms.referenceapp.photoapp.data.remote.ObjectTypeInfoHelper
import com.hms.referenceapp.photoapp.util.Event
import com.huawei.agconnect.cloud.database.*
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudDbRepository @Inject constructor(
    private val cloudDB: AGConnectCloudDB
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

    @ExperimentalCoroutinesApi
    fun saveToCloudDB(cloudDBZoneObject: CloudDBZoneObject): Flow<Result<Boolean>> = callbackFlow {
        trySend(Result.Loading)
        if (isDpOpen().not()) {
            trySend(Result.Error(Exception("Something went wrong")))
            close()
            return@callbackFlow
        }

        val upsertTask = cloudDBZone!!.executeUpsert(cloudDBZoneObject)
        upsertTask.addOnSuccessListener { cloudDBZoneResult ->
            Log.i("Saved Data Successfully", "Upsert $cloudDBZoneResult records")
            trySend(Result.Success(true))
        }.addOnFailureListener { exception ->
            trySend(Result.Error(exception))
            Log.i("Couldn't Save Data: ", exception.localizedMessage.orEmpty())
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
            val snapshotQuery = CloudDBZoneQuery.where(PhotoDetails::class.java)
                .equalTo(queryTitle, queryValue)
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
            Log.w(TAG, "CloudDBZone is null, try re-open it")
            return
        }
        val queryTask = cloudDBZone!!.executeQuery(
            CloudDBZoneQuery.where(User::class.java),
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        )
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
            Log.w(TAG, "CloudDBZone is null, try re-open it")
            _allSharedPhotosResponse.value = Event(Result.Error(Exception("Something went wrong")))
            return
        }
        cloudDBZone!!.executeCountQuery(
            CloudDBZoneQuery.where(Photos::class.java).equalTo("fileId", fileId), "id",
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        ).addOnSuccessListener { count ->
            val firstLimit = getLimit(count.toInt())
            getPhotosWithPagination(
                fileId = fileId,
                null,
                limit = firstLimit,
                onSuccessListener = {
                    val photos = getPhotosBySnapshot(it)
                    allSharedPhotos.addAll(photos)
                    _allSharedPhotosResponse.value = Event(Result.Success(photos))
                    remind = (count - firstLimit).toInt()
                    getMorePhotos(fileId)
                }, onErrorListener = {
                    _allSharedPhotosResponse.value = Event(Result.Error(it))
                })
        }.addOnFailureListener {
            _allSharedPhotosResponse.value = Event(Result.Error(it))
        }
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


    fun getPendingRequests(){
        if (cloudDBZone == null) {
            return
        }
        val queryTask = cloudDBZone!!.executeQuery(
            CloudDBZoneQuery.where(UserRelationship::class.java),
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
        )
        queryTask.addOnSuccessListener { snapshot -> processUserRelationQueryResult(snapshot)
        }
            .addOnFailureListener {

            }
    }

    private fun getLimit(count: Int) = (if (count >= 4) 4 else count)


    companion object {
        private const val DB_NAME = "PhotoAppDB"
        private const val TAG = "CloudDB"
    }
}