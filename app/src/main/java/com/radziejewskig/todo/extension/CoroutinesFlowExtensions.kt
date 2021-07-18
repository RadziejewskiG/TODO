package com.radziejewskig.todo.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.radziejewskig.todo.base.CommonState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

fun <T: CommonState> MutableLiveData<T>.toMutableStateFlow(): MutableStateFlow<T> = MutableStateFlow(value!!)

suspend fun <T> withMain(content: suspend CoroutineScope.() -> T) = withContext(Dispatchers.Main, content)
suspend fun <T> withIo(content: suspend CoroutineScope.() -> T) = withContext(Dispatchers.IO, content)

fun <T> Flow<T>.collectLatestWhenStarted(
    scope: LifecycleCoroutineScope,
    content: suspend CoroutineScope.(T) -> Unit
) {
    scope.launchWhenStarted {
        collectLatest {
            content(it)
        }
    }
}

fun <T> StateFlow<T>.collectLatestWhenStartedAutoCancelling(
    lifecycleOwner: LifecycleOwner,
    content: suspend CoroutineScope.(T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            collectLatest {
                content(it)
            }
        }
    }
}

fun <T> Flow<T>.collectWhenStartedAutoCancelling(
    scope: LifecycleCoroutineScope,
    lifecycle: Lifecycle,
    content: suspend CoroutineScope.(T) -> Unit
) {
    scope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            collect {
                content(it)
            }
        }
    }
}

fun Fragment.launchLifecycleScopeWhenStarted(
    onError: (Throwable) -> Unit = {},
    content: suspend CoroutineScope.() -> Unit
) = viewLifecycleOwner.lifecycleScope.launchSafeWhenStarted(
    content = {
        content()
    },
    onError = { error ->
        onError(error)
    }
)

fun LifecycleCoroutineScope.launchSafeWhenStarted(
    onError: (Throwable) -> Unit = {},
    content: suspend CoroutineScope.() -> Unit,
) = launchWhenStarted {
    try {
        content()
    } catch (e: Exception) {
        if(e is CancellationException) {
            throw e
        }
        onError(e)
    }
}

fun CoroutineScope.launchSafe(
    dispatcher: CoroutineContext,
    onError: suspend CoroutineScope.(Throwable) -> Unit = {},
    content: suspend CoroutineScope.() -> Unit,
) = launch (
    dispatcher
) {
    try {
        content()
    } catch (e: Exception) {
        if(e is CancellationException) {
            throw e
        }
        onError(e)
    }
}

fun AppCompatActivity.launchLifecycleScopeWhenStarted(
    delayMilliseconds: Long = 0,
    onError: (Throwable) -> Unit = {},
    content: suspend CoroutineScope.() -> Unit,
) = lifecycleScope.launchSafeWhenStarted (
    content = {
        delay(delayMilliseconds)
        content()
    },
    onError = { error ->
        onError(error)
    }
)
