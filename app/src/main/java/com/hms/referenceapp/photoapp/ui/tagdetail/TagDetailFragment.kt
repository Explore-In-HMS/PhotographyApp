/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.tagdetail

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.referenceapp.photoapp.R
import com.hms.referenceapp.photoapp.adapter.ClassificationPhotosAdapter
import com.hms.referenceapp.photoapp.databinding.FragmentTagDetailBinding
import com.hms.referenceapp.photoapp.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TagDetailFragment :
    BaseFragment<TagDetailViewModel, FragmentTagDetailBinding>(FragmentTagDetailBinding::inflate) {

    override val viewModel: TagDetailViewModel by viewModels()

    private val tagDetailFragmentArgs: TagDetailFragmentArgs by navArgs()

    @Inject
    lateinit var classificationPhotosAdapter: ClassificationPhotosAdapter

    override fun setupUi() {
        with(binding) {
            // Set Tag Name
            tagName.text = getString(R.string.tagName, tagDetailFragmentArgs.tagName)
        }

        loadData()
    }

    private fun loadData() {
        val classificationItems = viewModel.filterClassificationItemsByTagName(
            tagName = tagDetailFragmentArgs.tagName,
            classificationList = tagDetailFragmentArgs.classificationItems
        )

        with(binding) {
            recyclerviewClassificationItems.layoutManager = LinearLayoutManager(requireContext())
            classificationPhotosAdapter.setItems(
                tagName = tagDetailFragmentArgs.tagName,
                classificationItems = classificationItems
            )
            recyclerviewClassificationItems.adapter = classificationPhotosAdapter
        }
    }

    override fun setupListeners() {
        classificationPhotosAdapter.setOnItemClickListener {
            val action =
                TagDetailFragmentDirections.actionTagDetailFragmentToOpenImageFragment(it, null)
            findNavController().navigate(action)
        }
    }
}