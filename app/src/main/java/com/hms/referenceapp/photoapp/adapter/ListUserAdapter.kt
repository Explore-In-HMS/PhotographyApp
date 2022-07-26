/*
 *  Copyright 2022. Explore in HMS. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.referenceapp.photoapp.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hms.referenceapp.photoapp.data.model.UserSelectUiModel
import com.hms.referenceapp.photoapp.databinding.UserItemBinding
import com.hms.referenceapp.photoapp.util.ext.viewBinding
import javax.inject.Inject


class ListUserAdapter @Inject constructor() :
    RecyclerView.Adapter<ListUserAdapter.UserItemViewHolder>() {

    private var userList = listOf<UserSelectUiModel>()

    private var onUserSelectListener: ((UserSelectUiModel) -> Unit)? = null
    fun onUserSelectListener(listener: (UserSelectUiModel) -> Unit) {
        onUserSelectListener = listener
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ListUserAdapter.UserItemViewHolder {
        val binding = viewGroup.viewBinding(UserItemBinding::inflate)
        return UserItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListUserAdapter.UserItemViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount() = userList.size

    inner class UserItemViewHolder(private val binding: UserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserSelectUiModel) {
            with(binding) {
                simpleCheckBox.setOnCheckedChangeListener(null)
                simpleCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    user.isChecked = isChecked
                    onUserSelectListener?.invoke(user)
                }
                simpleCheckBox.isChecked = user.isChecked
                userName.text = user.user.name
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setUserList(userList: List<UserSelectUiModel>) {
        this.userList = userList
        notifyDataSetChanged()
    }
}