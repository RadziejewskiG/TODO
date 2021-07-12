package com.radziejewskig.todo.base

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.radziejewskig.todo.extension.launchSafe
import com.radziejewskig.todo.extension.showMessage
import com.radziejewskig.todo.extension.withMain
import com.radziejewskig.todo.utils.ErrorUtil
import com.radziejewskig.todo.utils.SafeHandleDelegate
import com.radziejewskig.todo.utils.data.MessageData
import com.radziejewskig.todo.utils.data.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

abstract class FragmentViewModel<BaseState: CommonState>(stateHandle: SavedStateHandle): BaseViewModel<BaseState>(stateHandle) {

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
                withMain {
                    showMessage(
                        MessageData(
                            MessageType.ERROR,
                            messageRes = ErrorUtil.getStringResForException(error)
                        )
                    )
                }
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
                withMain {
                    showMessage(
                        MessageData(
                            MessageType.ERROR,
                            messageRes = ErrorUtil.getStringResForException(error)
                        )
                    )
                }
            }
            onError(error)
        }
    )

}
