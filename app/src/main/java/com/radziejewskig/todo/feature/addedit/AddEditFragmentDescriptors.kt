package com.radziejewskig.todo.feature.addedit

import android.os.Parcelable
import com.radziejewskig.todo.base.CommonState
import com.radziejewskig.todo.base.SingleEvent
import com.radziejewskig.todo.data.model.Task
import kotlinx.parcelize.Parcelize

enum class AddEditTaskSingleEvent: SingleEvent {
    SHOW_ERRORS,
    NAVIGATE_BACK,
}

@Parcelize
data class AddEditFragmentState(
    var task: Task = Task(),
    var isEditing: Boolean = false
): CommonState()

@Parcelize
data class EditTaskErrors(
    val showTitleEmptyError: Boolean,
    val showTitleTooLongError: Boolean,
    val showDescriptionTooLongError: Boolean,
): Parcelable
