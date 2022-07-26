/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.util.ext

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.hms.referenceapp.photoapp.R
import com.hms.referenceapp.photoapp.custom.AppPermission
import com.hms.referenceapp.photoapp.custom.CustomDialog

fun Fragment.goSettings() {
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", requireContext().packageName, null)
    }.run {
        startActivity(this)
    }
}

fun Fragment.hasPermission(permission: AppPermission): Boolean {
    return requireContext().checkSelfPermission(
        permission.name
    ) == PackageManager.PERMISSION_GRANTED
}

fun Fragment.getPermissionResultLauncher(
    permission: AppPermission,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: ((AppPermission) -> Unit)? = null,
    onPermissionDeniedPermanently: ((AppPermission) -> Unit)? = null
) = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
    when {
        isGranted -> onPermissionGranted.invoke()
        shouldShowRequestPermissionRationale(permission.name).not() -> onPermissionDeniedPermanently?.invoke(
            permission
        )
        else -> onPermissionDenied?.invoke(permission)
    }
}

fun Fragment.showPermissionDialog(
    @StringRes message: Int,
    onClickListener: () -> Unit,
    isCancelable: Boolean = false
) {
    CustomDialog(requireContext())
        .setCancelable(isCancelable)
        .setTitle(getString(R.string.dialog_permission_title))
        .setMessage(getString(message))
        .setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_permission))
        .setPositiveButton(
            positiveText = getString(R.string.dialog_permission_button_positive),
            onClickListener = onClickListener
        )
        .createDialog()
        .show()
}

private fun isScreenPortrait(activity: Activity) =
    (activity.resources?.configuration?.orientation
        ?: Configuration.ORIENTATION_PORTRAIT) == Configuration.ORIENTATION_PORTRAIT

fun Fragment.getSpanCountByOrientation(): Int = if (activity?.let { isScreenPortrait(it) } == true) 4 else 8

fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), message, duration).show()
}