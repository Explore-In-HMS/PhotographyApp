/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hms.referenceapp.photoapp.databinding.FileItemBinding
import com.hms.referenceapp.photoapp.ui.shareimage.SharePhotoModel
import com.hms.referenceapp.photoapp.util.ext.viewBinding
import javax.inject.Inject

class SharedFileAdapter @Inject constructor() :
    ListAdapter<SharePhotoModel, SharedFileAdapter.FileItemViewHolder>(PhotoDiffCallback) {

    private var onItemClickListener: ((SharePhotoModel) -> Unit)? = null
    fun setOnItemClickListener(listener: (SharePhotoModel) -> Unit) {
        onItemClickListener = listener
    }

    inner class FileItemViewHolder(
        private val binding: FileItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(fileListItem: SharePhotoModel) {
            binding.titleTv.text = fileListItem.title
            binding.descriptionTv.text = fileListItem.description
            binding.sharedPersonCountBtn.text = fileListItem.sharedPersonCount

            binding.fileListCard.setOnClickListener {
                onItemClickListener?.invoke(fileListItem)
            }
        }
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): FileItemViewHolder {
        val binding = viewGroup.viewBinding(FileItemBinding::inflate)
        return FileItemViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FileItemViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    object PhotoDiffCallback : DiffUtil.ItemCallback<SharePhotoModel>() {
        override fun areItemsTheSame(oldItem: SharePhotoModel, newItem: SharePhotoModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: SharePhotoModel,
            newItem: SharePhotoModel
        ): Boolean {
            return oldItem.equals(newItem)
        }
    }
}