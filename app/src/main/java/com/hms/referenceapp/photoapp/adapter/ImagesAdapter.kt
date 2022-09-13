/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.adapter

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hms.referenceapp.photoapp.databinding.GalleryItemBinding
import com.hms.referenceapp.photoapp.util.ext.load
import com.hms.referenceapp.photoapp.util.ext.viewBinding
import javax.inject.Inject

class ImagesAdapter @Inject constructor() :
    ListAdapter<Bitmap, ImagesAdapter.GalleryItemViewHolder>(PhotoDiffCallback) {

    private var onItemClickListener: ((Bitmap) -> Unit)? = null
    fun setOnItemClickListener(listener: (Bitmap) -> Unit) {
        onItemClickListener = listener
    }

    inner class GalleryItemViewHolder(
        private val binding: GalleryItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(image: Bitmap) {
            binding.image.load(image)

            binding.image.setOnClickListener {
                onItemClickListener?.invoke(image)
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): GalleryItemViewHolder {
        val binding = viewGroup.viewBinding(GalleryItemBinding::inflate)
        return GalleryItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GalleryItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object PhotoDiffCallback : DiffUtil.ItemCallback<Bitmap>() {
        override fun areItemsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
            return newItem.equals(oldItem)
        }
    }


}