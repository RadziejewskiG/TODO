package com.radziejewskig.todo.feature.addedit

import android.os.Parcelable
import com.radziejewskig.todo.base.CommonEvent
import com.radziejewskig.todo.base.CommonState
import com.radziejewskig.todo.data.model.Task
import kotlinx.parcelize.Parcelize

sealed class AddEditTaskSingleEvent: CommonEvent {
    class ShowErrors(
        val editTaskErrors: EditTaskErrors
    ): AddEditTaskSingleEvent()
    object NavigateBack: AddEditTaskSingleEvent()
}

@Parcelize
data class AddEditFragmentState(
    val task: Task = Task(),
    val isEditing: Boolean = false,
    val changeOccurred: Boolean = false,
): CommonState()

@Parcelize
data class EditTaskErrors(
    val showTitleEmptyError: Boolean,
    val showTitleTooLongError: Boolean,
    val showDescriptionTooLongError: Boolean,
): Parcelable
