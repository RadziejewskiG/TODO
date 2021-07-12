package com.radziejewskig.todo.utils.data

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class MessageData(
    val type: MessageType,
    @StringRes
    val messageRes: Int? = null,
    val messageString: String = "",
    val durationShort: Boolean = false,
): Parcelable

enum class MessageType{
    ERROR,
    SUCCESS,
    WARNING,
}

fun MessageData.getMessageString(context: Context) = if (messageRes != null) {
    context.getString(messageRes)
} else{
    this.messageString
}