/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.adapter

import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hms.referenceapp.photoapp.data.model.ClassificationModel
import com.hms.referenceapp.photoapp.databinding.TagPhotoItemBinding
import com.hms.referenceapp.photoapp.util.ext.load
import com.hms.referenceapp.photoapp.util.ext.viewBinding
import javax.inject.Inject


class ClassificationPhotosAdapter @Inject constructor() :
    RecyclerView.Adapter<ClassificationPhotosAdapter.ViewHolder>() {

    private var classificationItems = listOf<ClassificationModel>()
    private var tagName: String? = null

    private var onItemClickListener: ((String) -> Unit)? = null
    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }

    inner class ViewHolder(
        binding: TagPhotoItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        val tagImageView = binding.themeTagImageView
        val tagTextViewFirst = binding.tagTextViewFirst
        val tagTextViewSecond = binding.tagTextViewSecond
        val tagItemCardView = binding.tagItemCard
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = viewGroup.viewBinding(TagPhotoItemBinding::inflate)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.tagImageView.load(classificationItems[position].imagePath)

        val parameterSize = classificationItems[position].resultParameters.size
        if (parameterSize == expectedTagCount) {
            val tagList = mutableListOf<String>()
            for (list in classificationItems[position].resultParameters) {
                if (list.tagName != tagName) {
                    tagList.add(list.tagName)
                }
            }
            holder.tagTextViewFirst.text = tagList[0]
            holder.tagTextViewSecond.text = tagList[1]
        } else {
            val tagList = mutableListOf<String>()
            for (list in classificationItems[position].resultParameters) {
                if (list.tagName != tagName) {
                    tagList.add(list.tagName)
                }
            }
            holder.tagTextViewFirst.text = tagList[0]
            holder.tagTextViewSecond.visibility = GONE
        }

        holder.tagItemCardView.setOnClickListener {
            onItemClickListener?.invoke(classificationItems[position].imagePath)
        }
    }

    override fun getItemCount(): Int {
        return classificationItems.size
    }

    fun setItems(tagName: String?, classificationItems: List<ClassificationModel>) {
        this.tagName = tagName
        this.classificationItems = classificationItems
    }

    companion object {
        const val expectedTagCount = 3
    }
}