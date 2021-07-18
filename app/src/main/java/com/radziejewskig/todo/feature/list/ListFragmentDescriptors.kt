package com.radziejewskig.todo.feature.list

import com.radziejewskig.todo.base.CommonState
import kotlinx.parcelize.Parcelize

@Parcelize
data class ListFragmentState(
    val isUpdating: Boolean = false
): CommonState()
