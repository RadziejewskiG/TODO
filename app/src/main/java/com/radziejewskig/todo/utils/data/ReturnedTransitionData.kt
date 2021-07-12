package com.radziejewskig.todo.utils.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReturnedTransitionData (
    val value: String
): Parcelable