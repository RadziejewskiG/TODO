package com.radziejewskig.todo.base

import android.os.Parcelable
import com.radziejewskig.todo.utils.data.MessageData
import kotlinx.parcelize.Parcelize

@Parcelize
open class CommonState: Parcelable

interface CommonEvent

class ShowMessageEvent(
    val messageData: MessageData
)