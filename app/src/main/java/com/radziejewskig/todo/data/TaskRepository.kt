package com.radziejewskig.todo.data

import com.radziejewskig.todo.data.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    fun getTasks(limit: Int): Flow<Triple<List<Task>?, Boolean, Exception?>>

    suspend fun addTask(task: Task): Boolean

    suspend fun editTask(task: Task): Boolean

    suspend fun deleteTask(task: Task): Boolean

    suspend fun changeTaskIsCompleted(task: Task): Boolean

}
