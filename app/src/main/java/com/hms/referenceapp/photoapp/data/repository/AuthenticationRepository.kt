/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.data.repository

import android.content.Intent
import com.hms.referenceapp.photoapp.common.Result
import com.hms.referenceapp.photoapp.data.model.user
import com.hms.referenceapp.photoapp.listeners.IServiceListener
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.AGConnectUser
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.result.AuthHuaweiId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationRepository @Inject constructor(
    private val agConnectAuth: AGConnectAuth,
    private val cloudDbRepository: CloudDbRepository,
) {
    fun getSignedInUser(data: Intent?, serviceListener: IServiceListener<AGConnectUser>) {
        getAuthHuaweiId(data)?.let { authHuaweiId ->
            val credential = HwIdAuthProvider.credentialWithToken(authHuaweiId.accessToken)
            agConnectAuth.signIn(credential)
                .addOnSuccessListener {
                    serviceListener.onSuccess(it.user)
                }
                .addOnFailureListener {
                    serviceListener.onError(it)
                }
        } ?: run {
            serviceListener.onError(Exception("Unexpected failure happen"))
        }
    }

    private fun getAuthHuaweiId(data: Intent?): AuthHuaweiId? {
        data?.let { signedInUser ->
            HuaweiIdAuthManager.parseAuthResultFromIntent(signedInUser)?.let { task ->
                return if (task.isSuccessful)
                    task.result
                else
                    null
            }
        } ?: run {
            return null
        }
    }

    @ExperimentalCoroutinesApi
    fun saveUserToCloud(user: user): Flow<Result<Boolean>> {
        return cloudDbRepository.saveToCloudDB(user)
    }

    fun signOut() {
        agConnectAuth.signOut()
    }
}