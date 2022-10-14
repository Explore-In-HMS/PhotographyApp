package com.hms.referenceapp.photoapp.data.model

data class PendingRequestUiModel(
    val id : String?,
    val name: String,
    var isAccepted: Boolean?,
    var isDeclined: Boolean?
)