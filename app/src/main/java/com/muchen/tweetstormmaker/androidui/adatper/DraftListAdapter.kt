package com.muchen.tweetstormmaker.androidui.adatper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.muchen.tweetstormandroid.databinding.ItemDraftBinding
import com.muchen.tweetstormmaker.interfaceadapter.model.Draft

class DraftListAdapter (val navigateToEditFragment: (NavController, Long) -> Unit)
    : ListAdapter<Draft, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemDraftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DraftViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as DraftViewHolder).bind(getItem(position))
    }

    inner class DraftViewHolder(val binding: ItemDraftBinding) :
        RecyclerView.ViewHolder(binding.root) {

        lateinit var draft: Draft
        fun bind(draftData: Draft) {
            draft = draftData
            binding.apply {
                textViewDraft.text = draftData.content
                root.setOnClickListener {
                    navigateToEditFragment(it.findNavController(), draftData.timeCreated)
                }
//                executePendingBindings()
            }
        }
    }
}

private class DiffCallback : DiffUtil.ItemCallback<Draft>() {
    override fun areItemsTheSame(oldItem: Draft, newItem: Draft): Boolean {
        return oldItem.timeCreated == newItem.timeCreated
    }

    override fun areContentsTheSame(oldItem: Draft, newItem: Draft): Boolean {
        return oldItem == newItem
    }
}