package com.radziejewskig.todo.utils

import com.radziejewskig.todo.data.model.Task
import com.radziejewskig.todo.extension.trimNormalize
import com.radziejewskig.todo.feature.addedit.AddEditFragment.Companion.DESCRIPTION_MAX_LENGTH
import com.radziejewskig.todo.feature.addedit.AddEditFragment.Companion.TITLE_MAX_LENGTH

object ValidationUtil {

    fun checkTaskValid(task: Task): Boolean {
        return when {
            task.title.isEmpty() -> false
            task.title.apply{ trimNormalize() }.length > TITLE_MAX_LENGTH -> false
            (task.description.apply{ trimNormalize() }?.length ?: 0) > DESCRIPTION_MAX_LENGTH -> false
            else -> true
        }
    }

}
