package com.radziejewskig.todo.feature.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.radziejewskig.todo.R
import com.radziejewskig.todo.base.CommonEvent
import com.radziejewskig.todo.base.FragmentViewModel
import com.radziejewskig.todo.data.TaskRepository
import com.radziejewskig.todo.data.model.Task
import com.radziejewskig.todo.di.AssistedSavedStateViewModelFactory
import com.radziejewskig.todo.extension.sameContentAs
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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class ListFragmentViewModel @AssistedInject constructor(
    private val taskRepository: TaskRepository,
    @Assisted private val handle: SavedStateHandle
): FragmentViewModel<ListFragmentState, CommonEvent>(handle) {

    @AssistedFactory
    interface Factory: AssistedSavedStateViewModelFactory<ListFragmentViewModel>

    override fun setupInitialState() = ListFragmentState()

    private val _tasks = MutableSharedFlow<List<TaskListItemModel>>(replay = 1)
    val tasks = _tasks.asSharedFlow()

    private var tasksJob: Job? = null
    private var justLoadingJob: Job? = null
    private var showNoInternetConnectionJob: Job? = null

    private var isDataFromCache: Boolean = false

    private var currentLimit = 0

    private var loadingNewPage: Boolean = false

    private var canTryLoadPage = true

    var loadingBarShowing: Boolean = false
    private set

    var changedItemId: String? = null
    private set

    var canShowEmptyImage: Boolean = true
    private set

    var previousList: List<Task> = listOf()

    override fun initialAct() {
        mutateState {
            copy(
                isUpdating = false
            )
        }
        tryLoadNewPage(isInitial = true)
    }

    private fun setIsUpdating(updating: Boolean) {
        mutateState {
            copy(
                isUpdating = updating
            )
        }
    }

    private fun setIsLoadingBarShowing(isShowing: Boolean) {
        loadingBarShowing = isShowing
    }

    private fun setIsLoadingNewPage(isLoading: Boolean) {
        loadingNewPage = isLoading
    }

    private fun setCanTryLoadNewPage(canTry: Boolean) {
        canTryLoadPage = canTry
    }

    fun resetChangedItemId() {
        changedItemId = null
    }

    fun changeTaskIsCompleted(task: Task) {
        if(!currentState().isUpdating) {
            setCanTryLoadNewPage(false)
            changedItemId = task.id
            launchLoading (
                content = {
                    taskRepository.changeTaskIsCompleted(task)
                    delay(100)
                    setCanTryLoadNewPage(true)
                },
                onError = {
                    resetChangedItemId()
                    setCanTryLoadNewPage(true)
                }
            )
        }
    }

    fun deleteTask(task: Task) {
        if(!currentState().isUpdating) {
            setCanTryLoadNewPage(false)
            launchLoading (
                content = {
                    taskRepository.deleteTask(task)
                    delay(100)
                    setCanTryLoadNewPage(true)
                },
                onError = {
                    setCanTryLoadNewPage(true)
                }
            )
        }
    }

    fun setCanShowEmptyImage(canShow: Boolean) {
        canShowEmptyImage = canShow
    }

    fun refresh() {
        if( (!currentState().isUpdating && !loadingNewPage) ||
            (isDataFromCache && loadingNewPage)
        ) {
            viewModelScope.launch {
                setCanShowEmptyImage(false)
                _tasks.emit(listOf())
                if(isDataFromCache && loadingNewPage) {
                    setIsLoadingNewPage(false)
                    setIsUpdating(false)
                }
                currentLimit = 0
                tryLoadNewPage(isInitial = true, showLoading = false)
            }
        }
    }

    fun resetLoadingStates() {
        setIsLoadingNewPage(false)
        setIsUpdating(false)
    }

    fun tryLoadNewPage(isInitial: Boolean = false, showLoading: Boolean = true) {
        if( canTryLoadPage &&
            !loadingNewPage &&
            !currentState().isUpdating
        ) {
            setIsLoadingNewPage(true)

            if(isInitial) {
                setIsUpdating(true)
            }
            setIsLoadingBarShowing(true)

            val cListSize = previousList.size

            // No more items to load so just show progress bar for a short time
            if(!previousList.isNullOrEmpty() && cListSize < currentLimit) {
                justLoadingJob = viewModelScope.launch {
                    val oldList = previousList.toList().map { TaskListItemModel.TaskItem(it) }
                    if(!previousList.isNullOrEmpty()) {
                        _tasks.emit(oldList+ listOf(TaskListItemModel.LoadingItem))
                    }
                    delay(300)

                    setIsLoadingBarShowing(false)

                    _tasks.emit(oldList)

                    setIsLoadingNewPage(false)
                    setIsUpdating(false)
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
                            if((loadingNewPage && !isFromCache) || !loadingNewPage) {
                                showNoInternetConnectionJob?.cancel()
                                justLoadingJob?.cancel()

                                setIsLoadingBarShowing(false)

                                val currentListSize = oldList.size

                                if(loadingNewPage) {
                                    if(taskList.size < currentListSize && !isInitial) {
                                        currentLimit -= PAGE_SIZE
                                    }
                                } else {
                                    setIsUpdating(true)
                                }

                                _tasks.emit(taskList.map { TaskListItemModel.TaskItem(it) })

                                if(taskList sameContentAs previousList) {
                                    setIsLoadingNewPage(false)
                                    setIsUpdating(false)
                                }
                                previousList = taskList
                            } else {
                                if (loadingNewPage && isFromCache) {
                                    showNoInternetConnectionJob?.cancel()
                                    showNoInternetConnectionJob = viewModelScope.launch {
                                        delay(2000)
                                        showMessage(
                                            MessageData(
                                                MessageType.ERROR,
                                                messageRes = R.string.error_check_internet_connection
                                            )
                                        )
                                    }
                                }
                            }

                        } else {
                            if(loadingNewPage) {
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
