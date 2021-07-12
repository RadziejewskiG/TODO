package com.radziejewskig.todo.feature.selectphoto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.radziejewskig.todo.R
import com.radziejewskig.todo.databinding.ItemPhotoBinding
import com.radziejewskig.todo.extension.loadImage

class PhotoAdapter(
    private val onItemClicked: (String) -> Unit,
): ListAdapter<String, PhotoAdapter.PhotoViewHolder>(photoComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PhotoViewHolder(ItemPhotoBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: PhotoAdapter.PhotoViewHolder, position: Int) {
        if (position >= 0) {
            val item = getItem(position)
            holder.bind(item)
        }
    }

    inner class PhotoViewHolder(
        private val binding: ItemPhotoBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                onItemClicked(getItem(bindingAdapterPosition))
            }
        }
        fun bind(imageUrl: String) = binding.run {
            val context = binding.root.context
            imv.loadImage(
                context,
                imageUrl,
                placeholderDrawableRes = R.drawable.ic_image
            )
        }
    }

    companion object {
        private val photoComparator = object: DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }
    }
}
