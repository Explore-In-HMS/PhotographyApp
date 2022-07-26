/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.util

import androidx.exifinterface.media.ExifInterface
import com.huawei.hms.maps.model.LatLng


class GeoDegreeConverter(exif: ExifInterface?) {
    var latLng: LatLng? = null
    var date : String? = null

    init {
        val latitude = exif?.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
        val latitudeRef = exif?.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
        val longitude = exif?.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
        val longitudeRef = exif?.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
        date = exif?.getAttribute(ExifInterface.TAG_DATETIME)
        if (latitude != null
            && latitudeRef != null
            && longitude != null
            && longitudeRef != null
        ) {
            val lat = if (latitudeRef == "N") {
                convertToDegree(latitude).toDouble()
            } else {
                (0 - convertToDegree(latitude)).toDouble()
            }
            val lng = if (longitudeRef == "E") {
                convertToDegree(longitude).toDouble()
            } else {
                (0 - convertToDegree(longitude)).toDouble()
            }

            latLng = LatLng(lat, lng)
        }
    }


    private fun convertToDegree(stringDMS: String): Float {
        val dms = stringDMS.split(",")
        val stringD = dms[0].split("/")
        val d0 = stringD[0].toDouble()
        val d1 = stringD[1].toDouble()
        val d = d0 / d1
        val stringM = dms[1].split("/")
        val m0 = stringM[0].toDouble()
        val m1 = stringM[1].toDouble()
        val m = m0 / m1
        val stringS = dms[2].split("/")
        val s0 = stringS[0].toDouble()
        val s1 = stringS[1].toDouble()
        val s = s0 / s1
        return (d + m / 60 + s / 3600).toFloat()
    }
}