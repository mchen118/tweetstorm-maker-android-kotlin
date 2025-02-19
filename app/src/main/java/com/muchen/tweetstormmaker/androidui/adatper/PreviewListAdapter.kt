package com.muchen.tweetstormmaker.androidui.adatper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.muchen.tweetstormandroid.databinding.ItemTweetPreviewBinding

class PreviewListAdapter : ListAdapter<String, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemTweetPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PreviewListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PreviewListViewHolder).bind(getItem(position))
    }

    inner class PreviewListViewHolder(val binding: ItemTweetPreviewBinding):
            RecyclerView.ViewHolder(binding.root) {
        fun bind(data: String) {
            binding.apply {
                textTweetPreview.text = data
            }
        }
    }

    private class DiffCallback: DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}