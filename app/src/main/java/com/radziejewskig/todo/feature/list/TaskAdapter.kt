package com.radziejewskig.todo.feature.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.radziejewskig.todo.R
import com.radziejewskig.todo.data.model.Task
import com.radziejewskig.todo.databinding.ItemLoadingBinding
import com.radziejewskig.todo.databinding.ItemTaskBinding
import com.radziejewskig.todo.extension.getColorFromRes
import com.radziejewskig.todo.extension.greyOut
import com.radziejewskig.todo.extension.loadImage
import com.radziejewskig.todo.extension.toDateText
import com.radziejewskig.todo.utils.data.TaskListItemModel

class TaskAdapter(
    private val onItemClicked: (Task, View) -> Unit,
    private val onItemLongClicked: (Task) -> Unit,
    private val onCheckboxClicked: (Task) -> Unit,
): ListAdapter<TaskListItemModel, RecyclerView.ViewHolder>(taskComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            TYPE_LOADING -> LoadingViewHolder(ItemLoadingBinding.inflate(inflater, parent, false))
            TYPE_ITEM -> TaskViewHolder(ItemTaskBinding.inflate(inflater, parent, false))
            else -> throw Exception("TaskAdapter onCreateViewHolder(): invalid view type")
        }
    }

    override fun getItemViewType(position: Int): Int = when(getItem(position)) {
        is TaskListItemModel.TaskItem -> TYPE_ITEM
        is TaskListItemModel.LoadingItem -> TYPE_LOADING
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val item = getItem(position)
        if(payloads.isEmpty() || item !is TaskListItemModel.TaskItem) {
            onBindViewHolder(holder, position)
        } else {
            if(payloads[0] == true) {
                (holder as TaskViewHolder).changeChecked(item.task.completed)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(position >= 0) {
            val item = getItem(position)
            when(holder) {
                is TaskViewHolder -> {
                    if (item is TaskListItemModel.TaskItem) {
                        holder.bind(item)
                    }
                }
                is LoadingViewHolder -> {
                    if (item is TaskListItemModel.LoadingItem) {
                        holder.bind(item)
                    }
                }
                else -> throw java.lang.Exception("TitleAdapter onBindViewHolder(): invalid item view type")
            }
        }
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val item = getItem(bindingAdapterPosition)
                if(item is TaskListItemModel.TaskItem) {
                    onItemClicked(item.task, binding.taskItemContent)
                }
            }
            itemView.setOnLongClickListener {
                val item = getItem(bindingAdapterPosition)
                if(item is TaskListItemModel.TaskItem) {
                    onItemLongClicked(item.task)
                    true
                } else false
            }
            binding.taskCbClickView.setOnClickListener {
                val item = getItem(bindingAdapterPosition)
                if(item is TaskListItemModel.TaskItem) {
                    onCheckboxClicked(item.task)
                }
            }
        }
        fun bind(item: TaskListItemModel.TaskItem) = binding.run {
            val context = binding.root.context
            val task = item.task

            icon.loadImage(
                context = context,
                imageUrl = task.iconUrl
            )

            taskItemContent.transitionName = getTaskEditSharedElementName(task)

            title.text = task.title
            description.text = task.description ?: ""
            description.isVisible = !task.description.isNullOrEmpty()

            tvDateTime.text = task.createdAt?.toDateText(context) ?: ""

            changeChecked(task.completed)
        }

        fun changeChecked(isChecked: Boolean) = binding.run {
            cb.isChecked = isChecked

            val color = binding.root.context.getColorFromRes(
                if (isChecked) {
                    R.color.black_lighter_alpha
                } else {
                    R.color.black_lighter
                }
            )

            if(isChecked) {
                icon.greyOut()
            } else {
                icon.clearColorFilter()
            }

            title.setTextColor(color)
            description.setTextColor(color)
        }
    }

    inner class LoadingViewHolder(
        private val binding: ItemLoadingBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TaskListItemModel.LoadingItem) = binding.run {
        }
    }

    companion object {
        fun getTaskEditSharedElementName(task: Task): String = "$SHARED_ELEMENTS_CONTENT_PREFIX${task.id}"

        private const val SHARED_ELEMENTS_CONTENT_PREFIX = "content:"

        private const val TYPE_ITEM = 1
        private const val TYPE_LOADING = 0

        private val taskComparator = object: DiffUtil.ItemCallback<TaskListItemModel>() {

            override fun areItemsTheSame(oldItem: TaskListItemModel, newItem: TaskListItemModel): Boolean =
                ((oldItem is TaskListItemModel.TaskItem && newItem is TaskListItemModel.TaskItem) && oldItem.task.id == newItem.task.id) ||
                (oldItem is TaskListItemModel.LoadingItem && newItem is TaskListItemModel.LoadingItem)

            override fun areContentsTheSame(oldItem: TaskListItemModel, newItem: TaskListItemModel): Boolean =
                oldItem == newItem

            override fun getChangePayload(oldItem: TaskListItemModel, newItem: TaskListItemModel): Any? {
                val shouldPassPayload = ((oldItem is TaskListItemModel.TaskItem && newItem is TaskListItemModel.TaskItem) && oldItem.task.completed != newItem.task.completed)
                return if(shouldPassPayload) true else null
            }

        }
    }
}
