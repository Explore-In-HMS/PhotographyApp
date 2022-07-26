/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.di

import android.content.Context
import com.huawei.agconnect.AGCRoutePolicy
import com.huawei.agconnect.AGConnectInstance
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.cloud.database.AGConnectCloudDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CloudDBModule {

    @Singleton
    @Provides
    fun provideAGConnectCloudDb(
        agConnectInstance: AGConnectInstance,
        agConnectAuth: AGConnectAuth
    ): AGConnectCloudDB {
        return AGConnectCloudDB.getInstance(
            agConnectInstance,
            agConnectAuth
        )
    }

    @Singleton
    @Provides
    fun provideAGConnectInstance(
        @ApplicationContext context: Context
    ): AGConnectInstance {
        val agcConnectOptions =
            AGConnectOptionsBuilder().setRoutePolicy(AGCRoutePolicy.GERMANY).build(context)
        return AGConnectInstance.buildInstance(agcConnectOptions)
    }


    @Singleton
    @Provides
    fun provideAGConnectAuth(
        agConnectInstance: AGConnectInstance
    ): AGConnectAuth {
        return AGConnectAuth.getInstance(agConnectInstance)
    }


}