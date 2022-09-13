/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.openimage

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hms.referenceapp.photoapp.R
import com.hms.referenceapp.photoapp.data.model.EditedPhotoModel
import com.hms.referenceapp.photoapp.databinding.FragmentOpenImageBinding
import com.hms.referenceapp.photoapp.ui.base.BaseFragment
import com.hms.referenceapp.photoapp.util.ext.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OpenImageFragment :
    BaseFragment<OpenImageViewModel, FragmentOpenImageBinding>(FragmentOpenImageBinding::inflate) {

    override val viewModel: OpenImageViewModel by viewModels()

    private val openImageFragmentArgs: OpenImageFragmentArgs by navArgs()
    private var openedImage: String? = null
    private var editedImage: Bitmap? = null
    private var editedImageUri: Uri? = null
    private var sharedImage: Bitmap? = null

    override fun setupUi() {
        openedImage = openImageFragmentArgs.imagePath
        editedImage = openImageFragmentArgs.editedImageArg?.editedImage
        sharedImage = openImageFragmentArgs.imageBitmap

        with(binding) {
            // Set openedImage if it available
            if (openedImage != null) {
                openedImage?.let { openedImageView.load(it) }
            } else if (sharedImage != null) {
                openedImageView.setImageBitmap(sharedImage)
                bottomNavigationView.visibility = View.INVISIBLE
            } else {
                // Set editedImage if it available
                checkAndSetEditedImage()
            }
        }
    }

    override fun setupObservers() {
        collectLast(flow = viewModel.saveImageResultFlow, action = ::saveImage)
        collectLast(flow = viewModel.editedImageUriFlow, action = ::editedImageUri)
    }

    override fun setupListeners() {
        with(binding) {
            // BottomNavigation View
            bottomNavigationView.setOnItemSelectedListener {
                return@setOnItemSelectedListener when (it.itemId) {
                    R.id.backToHomeFragment -> {
                        navigateToHomeFragment()
                        true
                    }
                    R.id.editImageFragment -> {
                        navigateToEditImageFragment()
                        true
                    }
                    R.id.shareImage -> {
                        shareImageAccordingToImageStatus()
                        true
                    }
                    else -> false
                }
            }

            // Buttons
            savePhotoButton.setOnClickListener {
                if (editedImage != null) {
                    binding.saveImageProgressBar.show()
                    viewModel.saveEditedPhoto(requireContext(), editedImage!!)
                }
            }
        }
    }


    private fun shareImageAccordingToImageStatus() {
        if (openedImage != null) {
            shareImage(openedImage!!.toUri())
            return
        }

        editedImageUri?.let {
            shareImage(it)
        } ?: run {
            showToast("Please save the edited image before sharing it.")
        }
    }


    private fun checkAndSetEditedImage() {
        if (editedImage != null) {
            binding.savePhotoButton.show()
            binding.openedImageView.setImageBitmap(editedImage)
        }
    }

    private fun saveImage(saveImageResult: Boolean?) {
        if (saveImageResult == true) {
            binding.saveImageProgressBar.gone()
            showToast("Edited Image Saved. Please Check Your Gallery.")
        } else if (saveImageResult == false) {
            showToast("Edited Image Not Saved.")
        }
    }

    private fun editedImageUri(uri: Uri?) {
        editedImageUri = uri
    }

    private fun shareImage(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(intent, "Share Image"))
    }

    private fun navigateToEditImageFragment() {
        if (openedImage != null) {
            val action = OpenImageFragmentDirections.actionOpenImageFragmentToEditImageFragment(
                openedImage
            )
            findNavController().navigate(action)
        } else if (editedImage != null) {
            val action =
                OpenImageFragmentDirections.actionOpenImageFragmentToEditImageFragment(null)
                    .setEditedImageArg(EditedPhotoModel(editedImage!!))
            findNavController().navigate(action)
        }
    }

    private fun navigateToHomeFragment() {
        findNavController().popBackStack()
    }

    companion object {
        const val ALBUM_NAME = "Photography App"
    }

}