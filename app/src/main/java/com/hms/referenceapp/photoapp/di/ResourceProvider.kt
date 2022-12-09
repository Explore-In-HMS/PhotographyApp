package com.hms.referenceapp.photoapp.di

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun getString(@StringRes resId: Int) = context.resources.getString(resId)

    fun getString(@StringRes resId: Int, vararg appendTexts: String) = context.resources.getString(resId, appendTexts)

}