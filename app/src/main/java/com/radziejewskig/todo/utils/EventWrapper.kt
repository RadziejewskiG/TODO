package com.radziejewskig.todo.utils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ParcelableString(val value: String): Parcelable

open class EventWrapper<out T>(private val content: T, val data: Parcelable = ParcelableString("")) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}
