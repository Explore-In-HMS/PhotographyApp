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
 * Definition of ObjectType PhotoDetails.
 *
 * @since 2022-07-23
 */
@PrimaryKeys({"id"})
public final class PhotoDetails extends CloudDBZoneObject {
    private Integer id;

    private String sender_id;

    private String sender_name;

    private String receiver_id;

    private String receiver_name;

    private String file_id;

    private String file_name;

    private String file_desc;

    private String number_of_people_shared;

    public PhotoDetails() {
        super(PhotoDetails.class);
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_name(String sender_name) {
        this.sender_name = sender_name;
    }

    public String getSender_name() {
        return sender_name;
    }

    public void setReceiver_id(String receiver_id) {
        this.receiver_id = receiver_id;
    }

    public String getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_name(String receiver_name) {
        this.receiver_name = receiver_name;
    }

    public String getReceiver_name() {
        return receiver_name;
    }

    public void setFile_id(String file_id) {
        this.file_id = file_id;
    }

    public String getFile_id() {
        return file_id;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_desc(String file_desc) {
        this.file_desc = file_desc;
    }

    public String getFile_desc() {
        return file_desc;
    }

    public void setNumber_of_people_shared(String number_of_people_shared) {
        this.number_of_people_shared = number_of_people_shared;
    }

    public String getNumber_of_people_shared() {
        return number_of_people_shared;
    }

}

