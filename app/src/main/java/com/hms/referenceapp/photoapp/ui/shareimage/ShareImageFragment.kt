/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.shareimage

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.referenceapp.photoapp.adapter.SharedFileAdapter
import com.hms.referenceapp.photoapp.data.model.FileInformationModel
import com.hms.referenceapp.photoapp.data.model.ParcelableUser
import com.hms.referenceapp.photoapp.databinding.FragmentShareImageBinding
import com.hms.referenceapp.photoapp.databinding.SharedPeopleDialogBinding
import com.hms.referenceapp.photoapp.ui.base.BaseFragment
import com.hms.referenceapp.photoapp.util.ext.collectLast
import com.hms.referenceapp.photoapp.util.ext.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.R as R1

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
        filesYouSharedAdapter.setOnSharedPersonItemClickListener {
            showSharedPeopleDialog(
                sharePhotoModel = it,
                true
            )
        }
        filesYouSharedAdapter.setOnDeleteItemClickListener { showDeleteFileDialog(it) }
        sharedFilesWithYouAdapter.setOnItemClickListener(::navigateShareImageDetailPage)
        sharedFilesWithYouAdapter.setOnSharedPersonItemClickListener {
            showSharedPeopleDialog(
                sharePhotoModel = it,
                false
            )
        }
    }

    override fun setupObservers() {
        collectLast(flow = viewModel.shareImageUiState, action = ::setUiState)
    }

    private fun navigateShareImageDetailPage(sharePhotoModel: SharePhotoModel) {
        val userList = arrayListOf<ParcelableUser>()
        val didIShare: Boolean
        val filesSharedWithYou = viewModel.getSharedWithYouPeopleFromFileId(sharePhotoModel.fileId)
        if (filesSharedWithYou?.isEmpty() == false) {
            didIShare = false
            filesSharedWithYou.forEach {
                userList.add(ParcelableUser(it.id, it.unionId, it.name))
            }
        } else {
            didIShare = true
            viewModel.getSharedPeopleFromFileId(sharePhotoModel.fileId)?.forEach {
                userList.add(ParcelableUser(it.id, it.unionId, it.name))
            }
        }
        with(sharePhotoModel) {
            SharePhotoModel(
                id = id,
                fileId = fileId,
                title = title,
                description = description,
                sharedPersonCount = sharedPersonCount,
                isFileSharedByMe = isFileSharedByMe
            )

            findNavController().navigate(
                ShareImageFragmentDirections.actionShareImageFragmentToShareImageDetailFragment(
                    sharePhotoModel,
                    userList.toTypedArray(),
                    didIShare
                )
            )
        }
    }

    private fun showSharedPeopleDialog(
        sharePhotoModel: SharePhotoModel,
        filesYouSharedStateValue: Boolean
    ) {
        val sharedUserList =
            if (filesYouSharedStateValue) viewModel.getSharedPeopleFromFileId(sharePhotoModel.fileId)
            else viewModel.getSharedWithYouPeopleFromFileId(sharePhotoModel.fileId)

        val sharedUserNameList = arrayListOf<String>()
        sharedUserList?.forEach {
            sharedUserNameList.add(it.name)
        }

        val li = LayoutInflater.from(context)
        val binding = SharedPeopleDialogBinding.inflate(li)
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
        alertDialogBuilder.setView(binding.root)

        binding.lvSharedPeople.adapter = ArrayAdapter(
            requireActivity(),
            R1.layout.simple_list_item_1, sharedUserNameList
        )


        val alertDialogSharedPeople = alertDialogBuilder.create()
        alertDialogSharedPeople.show()
        alertDialogSharedPeople.getWindow()?.setLayout(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        binding.button.setOnClickListener {
            alertDialogSharedPeople.dismiss()
        }
    }

    private fun showDeleteFileDialog(file: SharePhotoModel) {
        val deleteDialogBuilder = AlertDialog.Builder(context)
        deleteDialogBuilder.setMessage("Are you sure you want to delete this file?")
        deleteDialogBuilder.setCancelable(true)
        deleteDialogBuilder.setPositiveButton(
            "Yes"
        ) { dialog, _ ->
            viewModel.deleteSharedFile(file.id, file.sharedPersonCount.toInt())
            dialog.cancel()
        }
        deleteDialogBuilder.setNegativeButton(
            "No"
        ) { dialog, _ -> dialog.cancel() }
        val alertDelete = deleteDialogBuilder.create()
        alertDelete.show()
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