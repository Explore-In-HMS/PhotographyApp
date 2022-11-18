package com.hms.referenceapp.photoapp.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hms.referenceapp.photoapp.data.model.ParcelableUser
import com.hms.referenceapp.photoapp.databinding.ItemSharedUserBinding
import com.hms.referenceapp.photoapp.util.ext.setVisibility
import com.hms.referenceapp.photoapp.util.ext.viewBinding
import javax.inject.Inject

class SharedUsersAdapter @Inject constructor() :
    ListAdapter<ParcelableUser, SharedUsersAdapter.ViewHolder>(PhotoDiffCallback) {

    private var onItemClickListener: ((ParcelableUser) -> Unit)? = null
    fun setOnItemClickListener(listener: (ParcelableUser) -> Unit) {
        onItemClickListener = listener
    }

    private var didIShared: Boolean? = null
    fun setDidIShared(value: Boolean) {
        didIShared = value
    }

    inner class ViewHolder(
        private val binding: ItemSharedUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(userListItem: ParcelableUser) {
            with(binding){
                tvUserName.text = userListItem.name
                if(didIShared == true){
                    ivDelete.setVisibility(true)
                    ivDelete.setOnClickListener {
                        onItemClickListener?.invoke(userListItem)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = viewGroup.viewBinding(ItemSharedUserBinding::inflate)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    object PhotoDiffCallback : DiffUtil.ItemCallback<ParcelableUser>() {
        override fun areItemsTheSame(oldItem: ParcelableUser, newItem: ParcelableUser): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ParcelableUser,
            newItem: ParcelableUser
        ): Boolean {
            return oldItem == newItem
        }
    }
}