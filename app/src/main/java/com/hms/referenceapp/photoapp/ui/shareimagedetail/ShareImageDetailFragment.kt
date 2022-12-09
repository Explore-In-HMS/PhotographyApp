/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.shareimagedetail

import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.hms.referenceapp.photoapp.R
import com.hms.referenceapp.photoapp.adapter.ImagesAdapter
import com.hms.referenceapp.photoapp.adapter.SharedUsersAdapter
import com.hms.referenceapp.photoapp.data.model.ParcelableUser
import com.hms.referenceapp.photoapp.data.model.Photos
import com.hms.referenceapp.photoapp.databinding.FragmentShareImageDetailBinding
import com.hms.referenceapp.photoapp.databinding.SharedPeopleDialogDeleteBinding
import com.hms.referenceapp.photoapp.ui.base.BaseFragment
import com.hms.referenceapp.photoapp.util.Constant.PATH_IMAGE
import com.hms.referenceapp.photoapp.util.ext.collectLast
import com.hms.referenceapp.photoapp.util.ext.getSpanCountByOrientation
import com.hms.referenceapp.photoapp.util.ext.setVisibility
import com.hms.referenceapp.photoapp.util.ext.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareImageDetailFragment :
    BaseFragment<ShareImageDetailViewModel, FragmentShareImageDetailBinding>(
        FragmentShareImageDetailBinding::inflate
    ) {

    override val viewModel: ShareImageDetailViewModel by viewModels()

    @Inject
    lateinit var imagesAdapter: ImagesAdapter

    @Inject
    lateinit var sharedUsersAdapter: SharedUsersAdapter

    private lateinit var alertDialogSharedPeople: AlertDialog

    override fun setupUi() {
        binding.recyclerviewSharedImages.apply {
            adapter = imagesAdapter
            layoutManager = GridLayoutManager(requireContext(), getSpanCountByOrientation())
        }
    }

    override fun setupListeners() {

        val deletedPhotos: MutableList<Photos> = ArrayList()
        imagesAdapter.setOnCardClickListener { image, clicked ->
            if (clicked) {
                deletedPhotos.add(image)
            } else {
                deletedPhotos.remove(image)
            }
        }

        with(binding) {
            selectImageBtn.setOnClickListener {
                selectPhotosWithIntent.launch(PATH_IMAGE)
            }
            btnShareImage.setOnClickListener {
                viewModel.sharePhotos()
            }
            deleteImageButton.setOnClickListener {
                deleteSharedPhotos(deletedPhotos)
            }
        }

        imagesAdapter.setOnItemClickListener {
            val action =
                ShareImageDetailFragmentDirections.actionShareImageDetailFragmentToOpenImageFragment(
                    null,
                    it
                )
            findNavController().navigate(action)
        }
    }

    private fun deleteSharedPhotos(deletedPhotos: MutableList<Photos>) {
        if (deletedPhotos.size > 0) {
            viewModel.deleteSharedPhotos(deletedPhotos)
            showToast(getString(R.string.delete_photos_successfully))
        } else {
            showToast(getString(R.string.delete_photos_warning))
        }
    }

    override fun setupObservers() {
        collectLast(flow = viewModel.sharePhotoUiState, action = ::setSharePhotoUiState)
    }

    private fun setPhotos(listPhotos: List<Photos>) {
        imagesAdapter.submitList(listPhotos)
    }

    private fun setSharePhotoUiState(sharePhotoUiState: SharePhotoUiState) {
        setDialogAdapter(sharePhotoUiState.sharedUserList, sharePhotoUiState.didIShare)

        with(binding) {
            val isLoading = sharePhotoUiState.loading
            title.text = sharePhotoUiState.title
            description.text = sharePhotoUiState.description
            ivIconDetailScreen.setOnClickListener { showSharedPeopleDialog() }
            filePersonCountDetailScreen.text = sharePhotoUiState.sharedUserList.size.toString()
            progressBar.setVisibility(isVisible = isLoading)
            btnShareImage.isEnabled = isLoading.not()
            selectImageBtn.isEnabled = isLoading.not()
        }
        sharePhotoUiState.error?.let {
            showError(it)
            viewModel.errorShown()
        }

        if (sharePhotoUiState.isPhotosSharedSuccessfully) {
            showToast(getString(R.string.photos_shared_successfully))
            findNavController().popBackStack()
        }

        setPhotos(sharePhotoUiState.updatedPhotos)
    }

    private fun showError(errorMessage: String) {
        showToast(errorMessage)
    }

    private var selectPhotosWithIntent =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { result ->
            viewModel.setSelectedPhoto(result, requireContext().contentResolver)
        }

    private fun deleteSharedUser(user: ParcelableUser) {
        viewModel.deleteUserFromSharedFile(viewModel.sharePhotoUiState.value.fileId, user.id)
    }

    private fun setDialogAdapter(sharedUserList: List<ParcelableUser>, didIShare: Boolean){
        with(sharedUsersAdapter) {
            setDidIShared(didIShare)
            setOnItemClickListener(::deleteSharedUser)
            submitList(sharedUserList)
        }

        if(sharedUserList.isEmpty()){
            alertDialogSharedPeople.dismiss()
            findNavController().popBackStack()
        }
    }

    private fun showSharedPeopleDialog(){
        val li = LayoutInflater.from(context)
        val dialogBinding = SharedPeopleDialogDeleteBinding.inflate(li)

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
        alertDialogBuilder.setView(dialogBinding.root)
        dialogBinding.rvSharedPeople.adapter = sharedUsersAdapter

        alertDialogSharedPeople = alertDialogBuilder.create()
        alertDialogSharedPeople.show()
        alertDialogSharedPeople.window?.setLayout(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.button.setOnClickListener {
            alertDialogSharedPeople.dismiss()
        }
    }
}