package com.radziejewskig.todo.base

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class CommonState: Parcelable

interface SingleEvent

enum class CommonActivitySingleEvent: SingleEvent {
    SHOW_LOADING_DIALOG,
    HIDE_LOADING_DIALOG,
}

enum class CommonFragmentSingleEvent: SingleEvent {
    SHOW_MESSAGE,
}