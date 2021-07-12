package com.radziejewskig.todo.data.source.network

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.radziejewskig.todo.data.TaskRepository
import com.radziejewskig.todo.data.model.Task
import com.radziejewskig.todo.extension.trimNormalize
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
): TaskRepository {

    @ExperimentalCoroutinesApi
    override fun getTasks(limit: Int) = callbackFlow<Triple<List<Task>?, Boolean, Exception?>> {
        val query = firestore.collection("tasks")
            .orderBy("completed", Query.Direction.ASCENDING)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .orderBy("title", Query.Direction.ASCENDING)
            .limit(limit.toLong())
        val subscription = query.addSnapshotListener(
            MetadataChanges.INCLUDE
        ) { value, error ->
            val fromCache = value?.metadata?.isFromCache == true
            if (error == null) {
                val list = mutableListOf<Task>()
                value?.forEach { snapshot ->
                    if (snapshot.exists()) {
                        val item = snapshot.toObject<Task>().apply {
                            id = snapshot.id
                        }
                        list.add(item)
                    }
                }
                trySend(Triple(list, fromCache, error))
            } else {
                trySend(Triple(null, fromCache, error))
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun addTask(task: Task): Boolean = try {
        task.title = task.title.trimNormalize() ?: ""
        task.description = task.description.trimNormalize()

        firestore.runTransaction { transaction ->
            val newTaskRef = firestore.collection("tasks").document()
            transaction.set(newTaskRef, task)
            null
        }.await()
        true
    } catch (e: Exception) {
        throw e
    }

    override suspend fun editTask(task: Task): Boolean = try {
        task.title = task.title.trimNormalize() ?: ""
        task.description = task.description.trimNormalize()

        firestore.runTransaction { transaction ->
            val taskRef = firestore.collection("tasks").document(task.id)
            transaction.set(taskRef, task)
            null
        }.await()
        true
    } catch (e: Exception) {
        throw e
    }

    override suspend fun changeTaskIsCompleted(task: Task): Boolean = try {
        firestore.runTransaction { transaction ->
            val taskRef = firestore.collection("tasks").document(task.id)
            transaction.set(taskRef, task.apply { completed = !completed })
            null
        }.await()
        true
    } catch (e: Exception) {
        throw e
    }

    override suspend fun deleteTask(task: Task): Boolean = try {
        firestore.runTransaction { transaction ->
            val taskRef = firestore.collection("tasks").document(task.id)
            transaction.delete(taskRef)
            null
        }.await()
        true
    } catch (e: Exception) {
        throw e
    }

}
