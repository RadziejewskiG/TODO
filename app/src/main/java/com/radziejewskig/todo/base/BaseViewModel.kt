package com.radziejewskig.todo.base

import androidx.annotation.CallSuper
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.radziejewskig.todo.extension.toMutableStateFlow
import com.radziejewskig.todo.utils.CommonViewHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseViewModel<BaseState: CommonState>(private val savedStateHandle: SavedStateHandle): ViewModel() {

    protected val _state by lazy { savedStateHandle.getLiveData(STATE_BUNDLE_TAG, getStateClassFromType().newInstance()).toMutableStateFlow() }
    val state: StateFlow<StateWrapper<BaseState>> = _state

    fun stateValue() = state.value.state

    val StateFlow<StateWrapper<BaseState>>.current
        get() = this.value.state

    private val isInitialized = AtomicBoolean(false)

    // Indicator for loading dialog
    val isProcessingData = MutableStateFlow(false)

    fun setIsProcessingData(isProcessing: Boolean) {
        isProcessingData.value = isProcessing
    }

    @CallSuper
    open fun start() {
        if (isInitialized.compareAndSet(false, true)) {
            initialAct()
        }
    }

    protected open fun initialAct() = Unit

    @CallSuper
    open fun saveToBundle() {
        // Data must be saved to state because it will only update itself in the state if new content is assigned to it's .value. It will ignore it's content changes.
        savedStateHandle.set(STATE_BUNDLE_TAG, state.current)
    }

    private fun getStateClassFromType() = CommonViewHelper.getTypeArgument(
        javaClass,
        CommonState::class.java
    ) as Class<BaseState>

    protected fun StateFlow<StateWrapper<BaseState>>.mutate(mutation: BaseState.() -> Unit) {
        val currentState = _state.value.state
        mutation(currentState)
        val newStateWrapped = StateWrapper(currentState)
        _state.value = newStateWrapped
    }

    class StateWrapper<BaseState: CommonState>(var state: BaseState)
}

private const val STATE_BUNDLE_TAG: String = "viewModelState"
