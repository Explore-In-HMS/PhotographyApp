/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.data.model;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

/**
 * Definition of ObjectType Photos.
 *
 * @since 2022-07-19
 */
@PrimaryKeys({"id"})
public final class Photos extends CloudDBZoneObject {
    private Integer id;

    private String file_id;

    private byte[] byte_array_of_photo;

    public Photos() {
        super(Photos.class);
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setFile_id(String file_id) {
        this.file_id = file_id;
    }

    public String getFile_id() {
        return file_id;
    }

    public void setByte_array_of_photo(byte[] byte_array_of_photo) {
        this.byte_array_of_photo = byte_array_of_photo;
    }

    public byte[] getByte_array_of_photo() {
        return byte_array_of_photo;
    }

}
