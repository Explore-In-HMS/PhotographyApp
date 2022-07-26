/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.data.local

import androidx.room.*
import com.hms.referenceapp.photoapp.data.model.ClassificationModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassificationDao {

    @Query("SELECT * FROM classification_table")
    fun getClassification() : List<ClassificationModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(classification: ClassificationModel) : Long

    @Delete
    suspend fun delete(classification: ClassificationModel)

    @Query("DELETE FROM classification_table")
    fun deleteAllClassification()

}