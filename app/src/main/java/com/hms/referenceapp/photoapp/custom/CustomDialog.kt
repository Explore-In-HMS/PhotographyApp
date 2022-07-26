/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.custom


import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import com.hms.referenceapp.photoapp.databinding.CustomDialogBinding


class CustomDialog(context: Context) {
    private var builder: AlertDialog.Builder = AlertDialog.Builder(context)
    private lateinit var alertDialog: AlertDialog
    private val dialogBinding: CustomDialogBinding =
        CustomDialogBinding.inflate(LayoutInflater.from(context))

    fun setTitle(title: String): CustomDialog {
        with(dialogBinding.dialogTitle) {
            visibility = View.VISIBLE
            text = title
        }
        return this
    }

    fun setMessage(title: String): CustomDialog {
        with(dialogBinding.dialogMessage) {
            visibility = View.VISIBLE
            text = title
        }
        return this
    }

    fun setCancelable(isCancelable: Boolean): CustomDialog {
        builder.setCancelable(isCancelable)
        return this
    }

    fun setIcon(icon: Drawable?): CustomDialog {
        with(dialogBinding.dialogIcon) {
            visibility = View.VISIBLE
            setImageDrawable(icon)
        }
        return this
    }

    fun setCancelButton(negativeText: String): CustomDialog {
        with(dialogBinding.negativeButton) {
            visibility = View.VISIBLE
            text = negativeText
            setOnClickListener { dismissDialog() }
        }
        return this
    }

    fun setPositiveButton(
        positiveText: String,
        onClickListener: () -> Unit
    ): CustomDialog {
        with(dialogBinding.positiveButton) {
            visibility = View.VISIBLE
            text = positiveText
            setOnClickListener {
                onClickListener.invoke()
                dismissDialog()
            }
        }
        return this
    }

    fun setNegativeButton(
        negativeText: String,
        onClickListener: () -> Unit
    ): CustomDialog {
        with(dialogBinding.negativeButton) {
            visibility = View.VISIBLE
            text = negativeText
            setOnClickListener {
                onClickListener.invoke()
                dismissDialog()
            }
        }
        return this
    }

    fun createDialog(): CustomDialog {
        builder.setView(dialogBinding.root)
        alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return this
    }

    fun show() {
        if (!this::alertDialog.isInitialized) {
            createDialog()
        }
        alertDialog.show()
    }

    private fun dismissDialog() = alertDialog.dismiss()

}