package com.radziejewskig.todo.base

import androidx.annotation.CallSuper
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow

abstract class ActivityViewModel<BaseState: CommonState>(private val stateHandle: SavedStateHandle): BaseViewModel<BaseState>(stateHandle) {

    val canNavigate = MutableStateFlow(stateHandle.get("canNavigate") ?: true)

    val isStatusBarLight = MutableStateFlow(stateHandle.get("isStatusBarLight") ?: true)

    @CallSuper
    override fun saveToBundle() {
        super.saveToBundle()
        stateHandle.set("isStatusBarLight", isStatusBarLight.value)
        stateHandle.set("canNavigate", canNavigate.value)
    }

}
