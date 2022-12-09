/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.listuser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hms.referenceapp.photoapp.R
import com.hms.referenceapp.photoapp.adapter.ListUserAdapter
import com.hms.referenceapp.photoapp.data.model.FileInformationModel
import com.hms.referenceapp.photoapp.databinding.FragmentListUserBinding
import com.hms.referenceapp.photoapp.util.Constant.BUNDLE_KEY
import com.hms.referenceapp.photoapp.util.Constant.REQUEST_KEY
import com.hms.referenceapp.photoapp.util.ext.collectLast
import com.hms.referenceapp.photoapp.util.ext.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_list_user.*
import javax.inject.Inject


@AndroidEntryPoint
class ListUserFragment : DialogFragment() {

    override fun getTheme() = R.style.RoundedCornersDialog

    private var _binding: FragmentListUserBinding? = null

    private val binding get() = _binding!!

    val viewModel: ListUserViewModel by viewModels()

    @Inject
    lateinit var listUserAdapter: ListUserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setupObservers()
        setupListeners()
    }

    private fun setupUi() {
        setAdapter()
        viewModel.getUsers()
    }

    private fun setupObservers() {
        collectLast(flow = viewModel.listUserUiState, action = ::setUiState)
    }

    private fun setupListeners() {
        listUserAdapter.onUserSelectListener {
            viewModel.selectUser(it)
        }
        binding.saveValuesButton.setOnClickListener {
            val text = titleEditText.text
            val description = descriptionEditText.text
            val fileInformation = FileInformationModel(
                title = text.toString(),
                description = description.toString(),
                userList = viewModel.getSelectedUsers(),
                numberOfPeopleShared = viewModel.getSelectedUsers().count().toString()
            )
            viewModel.controlFileInformationModel(fileInformation)
            setFragmentResult(REQUEST_KEY, bundleOf(BUNDLE_KEY to fileInformation))
        }
    }

    private fun setUiState(listUserUiState: ListUserUiState) {
        with(listUserUiState){
            error?.let {
                showError(it)
                viewModel.errorShown()
            }
            savedUserList.let {
                listUserAdapter.setUserList(it)
            }
            loading.let {}
            if (shareImageInformationFileTaken) {
                findNavController().popBackStack()
            }
        }
    }

    private fun setAdapter(){
        binding.recyclerviewUserItems.adapter = listUserAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showError(errorMessage: String) {
        showToast(errorMessage)
    }
}