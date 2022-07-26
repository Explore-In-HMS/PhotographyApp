/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.ui.clustered

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.hms.referenceapp.photoapp.R
import com.hms.referenceapp.photoapp.adapter.ClusteredPhotosAdapter
import com.hms.referenceapp.photoapp.databinding.FragmentClusteredPhotosBinding
import com.hms.referenceapp.photoapp.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClusteredPhotosFragment :
    BaseFragment<ClusteredPhotosViewModel, FragmentClusteredPhotosBinding>(
        FragmentClusteredPhotosBinding::inflate
    ) {
    override val viewModel: ClusteredPhotosViewModel by viewModels()

    private val args: ClusteredPhotosFragmentArgs by navArgs()

    @Inject
    lateinit var clusteredItemsAdapter: ClusteredPhotosAdapter

    override fun setupUi() {
        viewModel.setClusteredItems(args.clusterItem, requireContext().contentResolver)
        with(binding) {
            val layoutManager = GridLayoutManager(requireContext(), 4)
            layoutManager.spanSizeLookup =
                object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (clusteredItemsAdapter.getItemViewType(position)) {
                            R.layout.date_item -> 4
                            R.layout.gallery_item -> 1
                            else -> -1
                        }
                    }
                }
            recyclerviewClusteredItems.layoutManager = layoutManager
            clusteredItemsAdapter.setClusteredItems(viewModel.items)
            recyclerviewClusteredItems.adapter = clusteredItemsAdapter
        }
    }
}