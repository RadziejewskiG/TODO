package com.radziejewskig.todo.base

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.radziejewskig.todo.extension.launchSafe
import com.radziejewskig.todo.utils.ErrorUtil
import com.radziejewskig.todo.utils.EventWrapper
import com.radziejewskig.todo.utils.SafeHandleDelegate
import com.radziejewskig.todo.utils.data.MessageData
import com.radziejewskig.todo.utils.data.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class FragmentViewModel<BaseState: CommonState, BaseEvent: CommonEvent>(stateHandle: SavedStateHandle): BaseViewModel<BaseState>(stateHandle) {

    private val _events = MutableSharedFlow<EventWrapper<BaseEvent>>(
        replay = 7,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()

    private val _messageEvent = MutableSharedFlow<EventWrapper<ShowMessageEvent>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messageEvent = _messageEvent.asSharedFlow()

    fun BaseEvent.emit() {
        val event = this
        viewModelScope.launch {
            _events.emit(EventWrapper(event))
        }
    }

    var shouldPostponeOnReturn: Boolean by SafeHandleDelegate(stateHandle, "shouldPostponeOnReturn", false)
    var shouldPostponeOnEnter: Boolean by SafeHandleDelegate(stateHandle, "shouldPostponeOnEnter", false)
    var enterTransitionAlreadyPostponed: Boolean by SafeHandleDelegate(stateHandle, "enterTransitionAlreadyPostponed", false)

    private var argsPassed: Boolean by SafeHandleDelegate(stateHandle, "argsPassed", false)

    protected fun setArgsPassed(actions: () -> Unit) {
        if (!argsPassed) {
            argsPassed = true
            actions()
        }
    }

    protected fun launchLoading(
        dispatcher: CoroutineContext = Dispatchers.IO,
        showErrorMessages: Boolean = true,
        onError: (Throwable) -> Unit = {},
        content: suspend CoroutineScope.() -> Unit,
    ) = viewModelScope.launchSafe(
        dispatcher = dispatcher,
        content = {
            setIsProcessingData(true)
            content()
            setIsProcessingData(false)
        },
        onError = { error ->
            if(showErrorMessages) {
                showMessage(
                    MessageData(
                        MessageType.ERROR,
                        messageRes = ErrorUtil.getStringResForException(error)
                    )
                )
            }
            onError(error)
            setIsProcessingData(false)
        }
    )

    protected fun launch(
        dispatcher: CoroutineContext = Dispatchers.IO,
        showErrorMessages: Boolean = true,
        onError: (Throwable) -> Unit = {},
        content: suspend CoroutineScope.() -> Unit,
    ) = viewModelScope.launchSafe(
        dispatcher = dispatcher,
        content = content,
        onError = { error ->
            if(showErrorMessages) {
                showMessage(
                    MessageData(
                        MessageType.ERROR,
                        messageRes = ErrorUtil.getStringResForException(error)
                    )
                )
            }
            onError(error)
        }
    )

    protected fun showMessage(showMessageData: MessageData) {
        viewModelScope.launch {
            _messageEvent.emit(EventWrapper(ShowMessageEvent(showMessageData)))
        }
    }

}
