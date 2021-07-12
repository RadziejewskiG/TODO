package com.radziejewskig.todo.extension

import com.radziejewskig.todo.base.CommonFragmentSingleEvent
import com.radziejewskig.todo.base.FragmentViewModel
import com.radziejewskig.todo.utils.EventWrapper
import com.radziejewskig.todo.utils.data.MessageData

fun FragmentViewModel<*>.showMessage(showMessageData: MessageData) {
    emitter.emit(EventWrapper(CommonFragmentSingleEvent.SHOW_MESSAGE, showMessageData))
}