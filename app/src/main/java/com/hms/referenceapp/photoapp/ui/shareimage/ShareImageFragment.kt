/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.shareimage

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.referenceapp.photoapp.adapter.SharedFileAdapter
import com.hms.referenceapp.photoapp.data.model.FileInformationModel
import com.hms.referenceapp.photoapp.databinding.FragmentShareImageBinding
import com.hms.referenceapp.photoapp.ui.base.BaseFragment
import com.hms.referenceapp.photoapp.util.ext.collectLast
import com.hms.referenceapp.photoapp.util.ext.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ShareImageFragment :
    BaseFragment<ShareImageViewModel, FragmentShareImageBinding>(FragmentShareImageBinding::inflate) {

    override val viewModel: ShareImageViewModel by viewModels()

    @Inject
    lateinit var filesYouSharedAdapter: SharedFileAdapter

    @Inject
    lateinit var sharedFilesWithYouAdapter: SharedFileAdapter

    private var fileInformationModel: FileInformationModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener("requestKey") { _, bundle ->
            fileInformationModel = bundle.getParcelable("bundleKey")
            Log.d("ShareImageFragment", "result : $fileInformationModel")

            viewModel.prepareFileData(fileInformationModel)
        }
    }

    override fun setupUi() {
        with(binding) {
            filesYouSharedListRv.adapter = filesYouSharedAdapter
            filesYouSharedListRv.layoutManager =
                LinearLayoutManager(requireContext())

            sharedFilesWithYouListRv.adapter = sharedFilesWithYouAdapter
            sharedFilesWithYouListRv.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun setupListeners() {
        binding.addFileButton.setOnClickListener {
            findNavController().navigate(ShareImageFragmentDirections.actionShareImageFragmentToListUserFragment())
        }

        filesYouSharedAdapter.setOnItemClickListener(::navigateShareImageDetailPage)
        sharedFilesWithYouAdapter.setOnItemClickListener(::navigateShareImageDetailPage)

    }

    override fun setupObservers() {
        collectLast(flow = viewModel.shareImageUiState, action = ::setUiState)
    }

    private fun navigateShareImageDetailPage(sharePhotoModel: SharePhotoModel) {
        with(sharePhotoModel) {
            SharePhotoModel(
                id = id,
                fileId = fileId,
                title = title,
                description = description,
                sharedPersonCount = sharedPersonCount
            )

            findNavController().navigate(
                ShareImageFragmentDirections.actionShareImageFragmentToShareImageDetailFragment(
                    sharePhotoModel
                )
            )
        }
    }

    private fun setUiState(shareImageUiState: ShareImageUiState) {
        shareImageUiState.error.firstOrNull()?.let {
            showError(it)
            viewModel.errorShown()
        }
        shareImageUiState.isSavedFilesWithPerson.firstOrNull()?.let {
            showToast("Saved Data Successfully")
            viewModel.savedFileWithPerson()
        }

        val filterFilesYouSharedList =
            viewModel.filterFilesYouSharedList(shareImageUiState.filesYouSharedList)
        filesYouSharedAdapter.submitList(filterFilesYouSharedList)

        val filterSharedFilesWithYouList =
            viewModel.filterSharedFilesWithYouList(shareImageUiState.sharedFilesWithYouList)
        sharedFilesWithYouAdapter.submitList(filterSharedFilesWithYouList)
    }

    private fun showError(errorMessage: String) {
        showToast(errorMessage)
    }
}