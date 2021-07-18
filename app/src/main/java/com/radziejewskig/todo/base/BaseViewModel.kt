package com.radziejewskig.todo.base

import androidx.annotation.CallSuper
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.radziejewskig.todo.extension.toMutableStateFlow
import com.radziejewskig.todo.utils.CommonViewHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseViewModel<State: CommonState>(private val savedStateHandle: SavedStateHandle): ViewModel() {

    private fun getStateClassFromType() = CommonViewHelper.getTypeArgument(
        javaClass,
        CommonState::class.java
    ) as Class<State>

    private val _state by lazy { savedStateHandle.getLiveData(STATE_BUNDLE_TAG, getStateClassFromType().newInstance()).toMutableStateFlow() }
    val state = _state.asStateFlow()

    val currentState: State = _state.value

    private val isInitialized = AtomicBoolean(false)

    // Indicator for loading dialog
    private val _isProcessingData = MutableStateFlow(false)
    val isProcessingData = _isProcessingData.asStateFlow()

    fun setIsProcessingData(isProcessing: Boolean) {
        _isProcessingData.value = isProcessing
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
        savedStateHandle.set(STATE_BUNDLE_TAG, currentState)
    }

    protected fun mutateState(mutation: State.() -> State) {
        val newState = currentState.mutation()
        _state.value = newState
    }

}

private const val STATE_BUNDLE_TAG: String = "viewModelState"
