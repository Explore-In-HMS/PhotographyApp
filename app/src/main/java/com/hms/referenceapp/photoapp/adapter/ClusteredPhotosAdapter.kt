/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hms.referenceapp.photoapp.R
import com.hms.referenceapp.photoapp.databinding.DateItemBinding
import com.hms.referenceapp.photoapp.databinding.GalleryItemBinding
import com.hms.referenceapp.photoapp.util.ext.load
import com.hms.referenceapp.photoapp.util.ext.viewBinding
import javax.inject.Inject

class ClusteredPhotosAdapter @Inject constructor() :
    RecyclerView.Adapter<ClusteredPhotosAdapter.HomeRecyclerViewHolder>() {

    private var photoList = emptyList<ClusteredPhotosViewItem>()

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): HomeRecyclerViewHolder {

        return when (viewType) {
            R.layout.date_item -> HomeRecyclerViewHolder.DateViewHolder(
                viewGroup.viewBinding(DateItemBinding::inflate)
            )
            R.layout.gallery_item -> HomeRecyclerViewHolder.ClusteredPhotosViewHolder(
                viewGroup.viewBinding(GalleryItemBinding::inflate)
            )

            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun onBindViewHolder(holder: HomeRecyclerViewHolder, position: Int) {
        when (holder) {
            is HomeRecyclerViewHolder.DateViewHolder -> holder.bind(photoList[position] as ClusteredPhotosViewItem.Date)
            is HomeRecyclerViewHolder.ClusteredPhotosViewHolder -> holder.bind(photoList[position] as ClusteredPhotosViewItem.Image)
        }
    }

    override fun getItemCount() = photoList.size

    override fun getItemViewType(position: Int): Int {
        return when (photoList[position]) {
            is ClusteredPhotosViewItem.Date -> R.layout.date_item
            is ClusteredPhotosViewItem.Image -> R.layout.gallery_item
        }
    }

    fun setClusteredItems(photoList: List<ClusteredPhotosViewItem>) {
        this.photoList = photoList
    }

    sealed class HomeRecyclerViewHolder(binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        class DateViewHolder(private val binding: DateItemBinding) :
            HomeRecyclerViewHolder(binding) {
            fun bind(date: ClusteredPhotosViewItem.Date) {
                binding.clusteredDateText.text = date.date
            }
        }

        class ClusteredPhotosViewHolder(private val binding: GalleryItemBinding) :
            HomeRecyclerViewHolder(binding) {
            fun bind(imagePath: ClusteredPhotosViewItem.Image) {
                imagePath.image?.let {
                    binding.image.load(it)
                }
            }
        }
    }
}