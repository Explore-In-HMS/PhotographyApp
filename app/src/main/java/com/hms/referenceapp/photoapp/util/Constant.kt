/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.util


object Constant {
    // Image Kit Initialize
    private const val PROJECT_ID = "99536292102062330"
    private const val APP_ID = "105761351"
    private const val API_KEY =
        "DAEDAO88/eanRuACeXLTShvrPLBUsjgXbqpLewy4YS8pQtXNhnO3DdvQ9UcxdU0bbyZihRmj3Hbsj/ZagbIGbk9Khwf3x/hh/uXIIQ=="
    private const val CLIENT_SECRET =
        "8958D6AE62FB06845557853F855EFFBAACA777A842650C8E79AF25F2A54D5286"
    private const val CLIENT_ID = "844138181556192576"

    const val jsonString =
        "{\"projectId\":\"${PROJECT_ID}\",\"appId\":\"$APP_ID\",\"authApiKey\":\"$API_KEY\",\"clientSecret\":\"$CLIENT_SECRET\",\"clientId\":\"$CLIENT_ID\",\"token\":\"tokenTest\"}"

    val filterTypeList = mapOf(
        "Black-and-white" to 1,
        "Brown tone" to 2,
        "Lazy" to 3,
        "Freesia" to 4,
        "Fuji" to 5,
        "Peach pink" to 6,
        "Sea salt" to 7,
        "Mint" to 8,
        "Reed" to 9,
        "Vintage" to 10,
        "Marshmallow" to 11,
        "Moss" to 12,
        "Sunlight" to 13,
        "Time" to 14,
        "Haze blue" to 15,
        "Sunflower" to 16,
        "Hard" to 17,
        "Bronze yellow" to 18,
        "Mono tone" to 19,
        "Yellow-green tone" to 20,
        "Yellow tone" to 21,
        "Green tone" to 22,
        "Cyan tone" to 23,
        "Violet tone" to 24,
    )

    // Share Image
    const val FILE_ID = "fileId"
    const val SENDER_ID = "senderId"
    const val RECEIVER_ID = "receiverId"
}