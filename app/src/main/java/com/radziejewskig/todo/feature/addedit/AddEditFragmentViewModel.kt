package com.radziejewskig.todo.feature.addedit

import androidx.lifecycle.SavedStateHandle
import com.radziejewskig.todo.base.FragmentViewModel
import com.radziejewskig.todo.data.TaskRepository
import com.radziejewskig.todo.data.model.Task
import com.radziejewskig.todo.di.AssistedSavedStateViewModelFactory
import com.radziejewskig.todo.extension.trimNormalize
import com.radziejewskig.todo.feature.addedit.AddEditFragment.Companion.DESCRIPTION_MAX_LENGTH
import com.radziejewskig.todo.feature.addedit.AddEditFragment.Companion.TITLE_MAX_LENGTH
import com.radziejewskig.todo.utils.ValidationUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay

@ExperimentalCoroutinesApi
class AddEditFragmentViewModel @AssistedInject constructor(
    private val taskRepository: TaskRepository,
    @Assisted private val handle: SavedStateHandle
): FragmentViewModel<AddEditFragmentState, AddEditTaskSingleEvent>(handle) {

    @AssistedFactory
    interface Factory: AssistedSavedStateViewModelFactory<AddEditFragmentViewModel>

    override fun setupInitialState() = AddEditFragmentState()

    fun setup(t: Task?) = setArgsPassed {
        if(t!= null) {
            mutateState {
                copy(
                    task = t,
                    isEditing = true
                )
            }
        }
    }

    fun titleChanged(newTitle: String) {
        if(newTitle != currentState().task.title) {
            mutateState {
                copy(
                    changeOccurred = true,
                    task = task.copy(
                        title = newTitle
                    )
                )
            }
        }
    }

    fun iconUrlChanged(newUrl: String?) {
        if(newUrl != currentState().task.iconUrl) {
            mutateState {
                copy(
                    changeOccurred = true,
                    task = task.copy(
                        iconUrl = newUrl
                    )
                )
            }
        }
    }

    fun descriptionChanged(newDescription: String?) {
        if(newDescription != currentState().task.description) {
            mutateState {
                copy(
                    changeOccurred = true,
                    task = task.copy(
                        description = newDescription
                    )
                )
            }
        }
    }

    fun addEditTask() {
        val task = currentState().task
        if(ValidationUtil.checkTaskValid(task)) {
            launchLoading {
                if(currentState().isEditing) {
                    taskRepository.editTask(task)
                } else {
                    taskRepository.addTask(task)
                }

                // Some delay just to show loading dialog for a while
                delay(300)

                AddEditTaskSingleEvent.NavigateBack.emit()
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
        AddEditTaskSingleEvent.ShowErrors(errorsData).emit()
    }

}
