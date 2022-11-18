package com.hms.referenceapp.photoapp.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ParcelableUser(
    val id: Long,
    val unionId: String?,
    val name: String
): Parcelable