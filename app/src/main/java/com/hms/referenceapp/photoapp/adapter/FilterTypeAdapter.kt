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
import com.hms.referenceapp.photoapp.databinding.FilterTypeItemBinding
import com.hms.referenceapp.photoapp.util.Constant.filterTypeList
import com.hms.referenceapp.photoapp.util.ext.viewBinding
import javax.inject.Inject


class FilterTypeAdapter @Inject constructor() :
    RecyclerView.Adapter<FilterTypeAdapter.ViewHolder>() {

    private var onItemClickListener: ((Int) -> Unit)? = null
    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): FilterTypeAdapter.ViewHolder {
        val binding = viewGroup.viewBinding(FilterTypeItemBinding::inflate)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterTypeAdapter.ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int {
        return filterTypeList.size
    }

    inner class ViewHolder(
        private val binding: FilterTypeItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(filterType: String) {
            binding.filterTypeText.text = filterType

            binding.filterCard.setOnClickListener {
                onItemClickListener?.invoke(bindingAdapterPosition)
            }
        }
    }

    private fun getItem(position: Int): String {
        return filterTypeList.keys.toList()[position]
    }
}