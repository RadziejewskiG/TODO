package com.radziejewskig.todo.utils.data

import com.radziejewskig.todo.data.model.Task

sealed class TaskListItemModel {
    data class TaskItem(val task: Task): TaskListItemModel()
    object LoadingItem: TaskListItemModel()
}