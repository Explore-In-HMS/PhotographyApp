/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2020. All rights reserved.
 * Generated by the CloudDB ObjectType compiler.  DO NOT EDIT!
 */
package com.hms.referenceapp.photoapp.data.model;

import com.huawei.agconnect.cloud.database.annotations.DefaultValue;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;
import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.Text;

import java.util.Date;

/**
 * Definition of ObjectType UserRelationship.
 *
 * @since 2022-12-08
 */
@PrimaryKeys({"firstSecondUID"})
public final class UserRelationship extends CloudDBZoneObject {
    private String firstUserId;

    private String secondUserId;

    @DefaultValue(booleanValue = false)
    private Boolean pendingFirstSecond;

    @DefaultValue(booleanValue = false)
    private Boolean pendingSecondFirst;

    @DefaultValue(booleanValue = false)
    private Boolean areFriends;

    private String firstSecondUID;

    private String firstUserName;

    private String secondUserName;

    public UserRelationship() {
        super(UserRelationship.class);
        this.pendingFirstSecond = false;
        this.pendingSecondFirst = false;
        this.areFriends = false;
    }

    public void setFirstUserId(String firstUserId) {
        this.firstUserId = firstUserId;
    }

    public String getFirstUserId() {
        return firstUserId;
    }

    public void setSecondUserId(String secondUserId) {
        this.secondUserId = secondUserId;
    }

    public String getSecondUserId() {
        return secondUserId;
    }

    public void setPendingFirstSecond(Boolean pendingFirstSecond) {
        this.pendingFirstSecond = pendingFirstSecond;
    }

    public Boolean getPendingFirstSecond() {
        return pendingFirstSecond;
    }

    public void setPendingSecondFirst(Boolean pendingSecondFirst) {
        this.pendingSecondFirst = pendingSecondFirst;
    }

    public Boolean getPendingSecondFirst() {
        return pendingSecondFirst;
    }

    public void setAreFriends(Boolean areFriends) {
        this.areFriends = areFriends;
    }

    public Boolean getAreFriends() {
        return areFriends;
    }

    public void setFirstSecondUID(String firstSecondUID) {
        this.firstSecondUID = firstSecondUID;
    }

    public String getFirstSecondUID() {
        return firstSecondUID;
    }

    public void setFirstUserName(String firstUserName) {
        this.firstUserName = firstUserName;
    }

    public String getFirstUserName() {
        return firstUserName;
    }

    public void setSecondUserName(String secondUserName) {
        this.secondUserName = secondUserName;
    }

    public String getSecondUserName() {
        return secondUserName;
    }

}
