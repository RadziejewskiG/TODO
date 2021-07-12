package com.radziejewskig.todo.feature.addedit

import androidx.lifecycle.SavedStateHandle
import com.radziejewskig.todo.base.FragmentViewModel
import com.radziejewskig.todo.data.TaskRepository
import com.radziejewskig.todo.data.model.Task
import com.radziejewskig.todo.di.AssistedSavedStateViewModelFactory
import com.radziejewskig.todo.extension.trimNormalize
import com.radziejewskig.todo.extension.withMain
import com.radziejewskig.todo.feature.addedit.AddEditFragment.Companion.DESCRIPTION_MAX_LENGTH
import com.radziejewskig.todo.feature.addedit.AddEditFragment.Companion.TITLE_MAX_LENGTH
import com.radziejewskig.todo.utils.ValidationUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

@ExperimentalCoroutinesApi
class AddEditFragmentViewModel @AssistedInject constructor(
    private val taskRepository: TaskRepository,
    @Assisted private val handle: SavedStateHandle
): FragmentViewModel<AddEditFragmentState>(handle) {

    @AssistedFactory
    interface Factory : AssistedSavedStateViewModelFactory<AddEditFragmentViewModel>

    val changeOccurred = MutableStateFlow(handle.get("changeOccurred") ?: false)

    fun setup(t: Task?) = setArgsPassed {
        val editing = t != null
        state.mutate {
            task = t ?: Task()
            isEditing = editing
        }
    }

    fun titleChanged(newTitle: String) {
        if(newTitle != stateValue().task.title) {
            setChangeOccurred()
            state.mutate {
                task = task.apply {
                    title = newTitle
                }
            }
        }
    }

    fun iconUrlChanged(newUrl: String?) {
        if(newUrl != stateValue().task.iconUrl) {
            setChangeOccurred()
            state.mutate {
                task = task.apply {
                    iconUrl = newUrl
                }
            }
        }
    }

    fun descriptionChanged(newDescription: String?) {
        if(newDescription != stateValue().task.description) {
            setChangeOccurred()
            state.mutate {
                task = task.apply {
                    description = newDescription
                }
            }
        }
    }

    private fun setChangeOccurred() {
        if(!changeOccurred.value) {
            changeOccurred.value = true
        }
    }

    fun addEditTask() {
        val task = stateValue().task
        if(ValidationUtil.checkTaskValid(task)) {
            launchLoading {
                if(stateValue().isEditing) {
                    taskRepository.editTask(task)
                } else {
                    taskRepository.addTask(task)
                }

                // Some delay just to show loading dialog for a while
                delay(300)

                withMain {
                    AddEditTaskSingleEvent.NAVIGATE_BACK.emit()
                }
            }
        } else {
            sendUIErrors(task)
        }
    }

    private fun sendUIErrors(task: Task) {
        val errorsData = EditTaskErrors (
            showTitleEmptyError = task.title.isEmpty(),
            showTitleTooLongError = task.title.apply { trimNormalize() }.length > TITLE_MAX_LENGTH,
            showDescriptionTooLongError = (task.description?.apply { trimNormalize() }?.length ?: 0) > DESCRIPTION_MAX_LENGTH
        )
        AddEditTaskSingleEvent.SHOW_ERRORS.emit(errorsData)
    }

    override fun saveToBundle() {
        super.saveToBundle()
        handle.set("changeOccurred", changeOccurred.value)
    }

}
