/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.editimage

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.MotionEvent
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.referenceapp.photoapp.adapter.FilterTypeAdapter
import com.hms.referenceapp.photoapp.data.model.EditedPhotoModel
import com.hms.referenceapp.photoapp.databinding.FragmentEditImageBinding
import com.hms.referenceapp.photoapp.ui.base.BaseFragment
import com.hms.referenceapp.photoapp.util.Constant.filterTypeList
import com.hms.referenceapp.photoapp.util.ext.collectLast
import com.hms.referenceapp.photoapp.util.ext.gone
import com.hms.referenceapp.photoapp.util.ext.load
import com.hms.referenceapp.photoapp.util.ext.show
import com.huawei.hms.image.vision.ImageVisionImpl
import com.huawei.hms.image.vision.crop.CropLayoutView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class EditImageFragment :
    BaseFragment<EditImageViewModel, FragmentEditImageBinding>(FragmentEditImageBinding::inflate) {

    override val viewModel: EditImageViewModel by viewModels()

    @Inject
    lateinit var imageVisionFilterAPI: ImageVisionImpl

    @Inject
    lateinit var filterTypeAdapter: FilterTypeAdapter

    private val editImageFragmentArgs: EditImageFragmentArgs by navArgs()

    private var bitmapImage: Bitmap? = null
    private var finalImage: Bitmap? = null

    private var isLongPressed = false

    override fun setupUi() {
        bitmapImage = if (editImageFragmentArgs.imagePath != null) {
            viewModel.convertToBitmap(
                contentResolver = requireContext().contentResolver,
                imagePath = editImageFragmentArgs.imagePath!!
            )
        } else {
            editImageFragmentArgs.editedImageArg?.editedImage
        }

        with(binding) {
            // Set Image
            editImageView.setImageBitmap(bitmapImage!!)
        }

        setupImageKit()
        loadFilterType()
    }

    override fun setupObservers() {
        collectLast(flow = viewModel.imageKitResponseFlow, action = ::loadFilteredImage)
    }

    override fun onDestroy() {
        super.onDestroy()
        imageVisionFilterAPI.stop()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setupListeners() {
        with(binding) {
            crop.setOnClickListener {
                clearFilterEdit()
                enableCropLayoutView()
                editImageInCropLayoutView()
                cropLayoutView.setAutoZoomEnabled(true)
                cropLayoutView.cropShape = CropLayoutView.CropShape.RECTANGLE
            }

            rotate.setOnClickListener {
                clearFilterEdit()
                enableCropLayoutView()
                editImageInCropLayoutView()
                cropLayoutView.rotateClockwise()
            }

            flipHorizontal.setOnClickListener {
                clearFilterEdit()
                enableCropLayoutView()
                editImageInCropLayoutView()
                cropLayoutView.flipImageHorizontally()
            }

            flipVertical.setOnClickListener {
                clearFilterEdit()
                enableCropLayoutView()
                editImageInCropLayoutView()
                cropLayoutView.flipImageVertically()
            }

            // Filter Button
            filterTypeAdapter.setOnItemClickListener { position ->
                if (finalImage != null) {
                    clearCropEdit()
                    enableFilterView()
                    viewModel.startFilter(
                        1.toFloat(),
                        1.toFloat(),
                        filterTypeList.values.toList()[position],
                        finalImage
                    )

                } else {
                    clearCropEdit()
                    enableFilterView()
                    viewModel.startFilter(
                        1.toFloat(),
                        1.toFloat(),
                        filterTypeList.values.toList()[position],
                        bitmapImage
                    )

                }
            }

            // If the user click Save Button, final image will be cropped image
            cropButton.setOnClickListener {
                finalImage = cropLayoutView.croppedImage
                clearCropEdit()
                enableEditImageView()
                editImageInImageView()
                enableEndEditButton()
            }

            cancelButton.setOnClickListener {
                clearAllEdit()
                editImageInImageView()
                enableEndEditButton()
            }

            editImageView.setOnLongClickListener {
                showOriginalImage()
                return@setOnLongClickListener true
            }

            editImageView.setOnTouchListener { view, motionEvent ->
                view.onTouchEvent(motionEvent)
                if (motionEvent.action == MotionEvent.ACTION_UP){
                   showEditedImage()
                }
                return@setOnTouchListener true
            }

            endEditButton.setOnClickListener {
                if (finalImage != null) {
                    val action =
                        EditImageFragmentDirections.actionEditImageFragmentToOpenImageFragment(null, null)
                            .setEditedImageArg(
                                finalImage?.let { image -> EditedPhotoModel(image) }
                            )
                    findNavController().navigate(action)
                } else {
                    val action =
                        EditImageFragmentDirections.actionEditImageFragmentToOpenImageFragment(
                            editImageFragmentArgs.imagePath, null
                        ).setEditedImageArg(bitmapImage?.let { image -> EditedPhotoModel(image) })
                    findNavController().navigate(action)
                }

            }
        }
    }

    private fun enableCropLayoutView() {
        with(binding) {
            cropLayoutView.show()
            cropButton.show()
            cancelButton.show()
            endEditButton.gone()
        }
    }

    private fun enableFilterView() {
        with(binding) {
            editImageView.show()
            filterButton.show()
            cancelButton.show()
            endEditButton.gone()
        }
    }

    private fun enableEndEditButton() {
        binding.endEditButton.show()
    }

    private fun enableEditImageView() {
        binding.editImageView.show()
    }

    private fun clearFilterEdit() {
        with(binding) {
            filterButton.gone()
            cancelButton.gone()
            editImageView.gone()
        }
    }

    private fun clearCropEdit() {
        with(binding) {
            cropButton.gone()
            cancelButton.gone()
            cropLayoutView.gone()
        }
    }

    private fun clearAllEdit() {
        with(binding) {
            filterButton.gone()
            cropButton.gone()
            cropLayoutView.gone()
            cancelButton.gone()
            editImageView.show()
        }
    }

    private fun editImageInImageView() {
        if (finalImage != null) {
            binding.editImageView.setImageBitmap(finalImage)
        } else {
            binding.editImageView.setImageBitmap(bitmapImage)
        }
    }

    private fun editImageInCropLayoutView() {
        if (finalImage != null) {
            binding.cropLayoutView.setImageBitmap(finalImage)
        } else {
            binding.cropLayoutView.setImageBitmap(bitmapImage)
        }
    }

    private fun setupImageKit() {
        viewModel.initImageKitService(requireContext())
    }

    private fun loadFilterType() {
        with(binding) {
            recyclerviewFilterTypes.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            recyclerviewFilterTypes.adapter = filterTypeAdapter
        }
    }

    private fun loadFilteredImage(filteredImage: Bitmap?) {
        with(binding) {
            // Set Filtered Image
            if (filteredImage != null) {
                editImageView.load(filteredImage)

                // If the user click Save Button, final image will be filtered image
                filterButton.setOnClickListener {
                    finalImage = filteredImage
                    clearFilterEdit()
                    enableEditImageView()
                    editImageInImageView()
                    enableEndEditButton()
                }
            }
        }
    }

    private fun showOriginalImage(){
        isLongPressed = true
        binding.editImageView.setImageBitmap(bitmapImage!!)
    }

    private fun showEditedImage(){
        if (isLongPressed){
            isLongPressed = false
            if (finalImage != null) {
                binding.editImageView.setImageBitmap(finalImage)
            }
        }
    }
}