package com.hms.referenceapp.photoapp.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hms.referenceapp.photoapp.data.model.PendingRequestUiModel
import com.hms.referenceapp.photoapp.data.model.UserSelectUiModel
import com.hms.referenceapp.photoapp.databinding.PendingRequestItemBinding
import com.hms.referenceapp.photoapp.util.ext.viewBinding
import javax.inject.Inject

class PendingRequestAdapter @Inject constructor() :
    RecyclerView.Adapter<PendingRequestAdapter.PendingRequestItemViewHolder>() {

    private var pendingRequestList = mutableListOf<PendingRequestUiModel>()

    private var onRequestUpdateListener: ((PendingRequestUiModel) -> Unit)? = null
    fun onRequestUpdateListener(listener: (PendingRequestUiModel) -> Unit) {
        onRequestUpdateListener = listener
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): PendingRequestAdapter.PendingRequestItemViewHolder {
        val binding = viewGroup.viewBinding(PendingRequestItemBinding::inflate)
        return PendingRequestItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PendingRequestAdapter.PendingRequestItemViewHolder, position: Int) {
        val pendingRequest = pendingRequestList[position]
        holder.bind(pendingRequest)
    }

    override fun getItemCount() = pendingRequestList.size

    inner class PendingRequestItemViewHolder(private val binding: PendingRequestItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pendingRequest: PendingRequestUiModel) {
            with(binding) {
                btnAccept.setOnClickListener {
                    pendingRequest.isAccepted = true
                    onRequestUpdateListener?.invoke(pendingRequest)
                }
                btnDecline.setOnClickListener {
                    pendingRequest.isDeclined = true
                    onRequestUpdateListener?.invoke(pendingRequest)
                }
                userName.text = pendingRequest.name
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setRequestList(pendingRequestList: MutableList<PendingRequestUiModel>) {
        this.pendingRequestList = pendingRequestList
        notifyDataSetChanged()
    }
}