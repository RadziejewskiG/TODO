package com.radziejewskig.todo.feature.list

import com.radziejewskig.todo.base.CommonState
import kotlinx.parcelize.Parcelize

@Parcelize
data class ListFragmentState(
    var isUpdating: Boolean = false,
    var loadingNewPage: Boolean = false,
    var loadingBarShowing: Boolean = false
): CommonState()
