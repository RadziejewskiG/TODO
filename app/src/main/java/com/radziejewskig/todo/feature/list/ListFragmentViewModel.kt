package com.radziejewskig.todo.feature.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.radziejewskig.todo.R
import com.radziejewskig.todo.base.FragmentViewModel
import com.radziejewskig.todo.data.TaskRepository
import com.radziejewskig.todo.data.model.Task
import com.radziejewskig.todo.di.AssistedSavedStateViewModelFactory
import com.radziejewskig.todo.extension.sameContentAs
import com.radziejewskig.todo.extension.showMessage
import com.radziejewskig.todo.extension.withMain
import com.radziejewskig.todo.utils.ErrorUtil
import com.radziejewskig.todo.utils.data.MessageData
import com.radziejewskig.todo.utils.data.MessageType
import com.radziejewskig.todo.utils.data.TaskListItemModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class ListFragmentViewModel @AssistedInject constructor(
    private val taskRepository: TaskRepository,
    @Assisted private val handle: SavedStateHandle
): FragmentViewModel<ListFragmentState>(handle) {

    @AssistedFactory
    interface Factory: AssistedSavedStateViewModelFactory<ListFragmentViewModel>

    private val _tasks = MutableSharedFlow<List<TaskListItemModel>>(replay = 1)
    val tasks: SharedFlow<List<TaskListItemModel>> = _tasks

    private var tasksJob: Job? = null
    private var justLoadingJob: Job? = null
    private var showNoInternetConnectionJob: Job? = null

    private var isDataFromCache: Boolean = false

    private var currentLimit = 0

    var previousList: List<Task> = listOf()

    private var canTryLoadPage = true

    var canShowEmptyImage = true

    var changedItemId: String? = null

    override fun initialAct() {
        state.mutate {
            loadingNewPage = false
            isUpdating = false
            loadingBarShowing = false
        }

        tryLoadNewPage(isInitial = true)
    }

    fun changeTaskIsCompleted(task: Task) {
        if(!stateValue().isUpdating) {
            canTryLoadPage = false
            changedItemId = task.id
            launchLoading (
                content = {
                    taskRepository.changeTaskIsCompleted(task)
                    delay(100)
                    canTryLoadPage = true
                },
                onError = {
                    changedItemId = null
                    canTryLoadPage = true
                }
            )
        }
    }

    fun deleteTask(task: Task) {
        if(!stateValue().isUpdating) {
            canTryLoadPage = false
            launchLoading (
                content = {
                    taskRepository.deleteTask(task)
                    delay(100)
                    canTryLoadPage = true
                },
                onError = {
                    canTryLoadPage = true
                }
            )
        }
    }

    fun refresh() {
        if( (!stateValue().isUpdating && !stateValue().loadingNewPage) ||
            (isDataFromCache && stateValue().loadingNewPage)
        ) {
            viewModelScope.launch {
                canShowEmptyImage = false
                _tasks.emit(listOf())
                if(isDataFromCache && stateValue().loadingNewPage) {
                    state.mutate {
                        loadingNewPage = false
                        isUpdating = false
                    }
                }
                currentLimit = 0
                tryLoadNewPage(isInitial = true, showLoading = false)
            }
        }
    }

    fun resetLoadingStates() {
        state.mutate {
            loadingNewPage = false
            isUpdating = false
        }
    }

    fun tryLoadNewPage(isInitial: Boolean = false, showLoading: Boolean = true) {
        if( canTryLoadPage &&
            !stateValue().loadingNewPage &&
            !stateValue().isUpdating
        ) {
            state.mutate {
                loadingNewPage = true
                if(isInitial) {
                    isUpdating = true
                }
                loadingBarShowing = true
            }

            val cListSize = previousList.size

            // No more items to load so just show progress bar for a short time
            if(!previousList.isNullOrEmpty() && cListSize < currentLimit) {
                justLoadingJob = viewModelScope.launch {
                    val oldList = previousList.toList().map { TaskListItemModel.TaskItem(it) }
                    if(!previousList.isNullOrEmpty()) {
                        _tasks.emit(oldList+ listOf(TaskListItemModel.LoadingItem))
                    }
                    delay(300)

                    state.mutate { loadingBarShowing = false }

                    _tasks.emit(oldList)

                    state.mutate {
                        loadingNewPage = false
                        isUpdating = false
                    }
                }
            } else {
                tasksJob?.cancel()

                currentLimit += PAGE_SIZE

                tasksJob = viewModelScope.launch {
                    val oldList = previousList.toList().map { TaskListItemModel.TaskItem(it) }

                    if(showLoading)  {
                        if(!previousList.isNullOrEmpty()) {
                            _tasks.emit(oldList + listOf(TaskListItemModel.LoadingItem))
                        }
                    }

                    delay(300)

                    taskRepository.getTasks(currentLimit).collect { response ->
                        val taskList = response.first
                        val isFromCache = response.second
                        val error = response.third

                        isDataFromCache = isFromCache

                        if(taskList != null) {
                            if((stateValue().loadingNewPage && !isFromCache) || !stateValue().loadingNewPage) {
                                showNoInternetConnectionJob?.cancel()
                                justLoadingJob?.cancel()

                                state.mutate { loadingBarShowing = false }

                                val currentListSize = oldList.size

                                if(stateValue().loadingNewPage) {
                                    if(taskList.size < currentListSize && !isInitial) {
                                        currentLimit -= PAGE_SIZE
                                    }
                                } else {
                                    state.mutate { isUpdating = true }
                                }

                                _tasks.emit(taskList.map { TaskListItemModel.TaskItem(it) })

                                if(taskList sameContentAs previousList) {
                                    state.mutate {
                                        loadingNewPage = false
                                        isUpdating = false
                                    }
                                }
                                previousList = taskList
                            } else {
                                if (stateValue().loadingNewPage && isFromCache) {
                                    showNoInternetConnectionJob?.cancel()
                                    showNoInternetConnectionJob = viewModelScope.launch {
                                        delay(2000)
                                        withMain {
                                            showMessage(MessageData(MessageType.ERROR, messageRes = R.string.error_check_internet_connection))
                                        }
                                    }
                                }
                            }

                        } else {
                            if(stateValue().loadingNewPage) {
                                withMain {
                                    showMessage(
                                        MessageData(
                                            MessageType.ERROR,
                                            messageRes = ErrorUtil.getStringResForException(error)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        showNoInternetConnectionJob?.cancel()
        justLoadingJob?.cancel()
        tasksJob?.cancel()
        super.onCleared()
    }

    companion object {
        private const val PAGE_SIZE: Int = 30
    }

}
