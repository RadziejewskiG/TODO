package com.radziejewskig.todo.feature.takephoto

import android.os.Parcelable
import com.radziejewskig.todo.base.CommonState
import com.radziejewskig.todo.base.SingleEvent
import kotlinx.parcelize.Parcelize

enum class TakePhotoEvent: SingleEvent {
    IMAGE_SAVED
}

@Parcelize
data class TakePhotoState(
    var currentPhotoPath: String = ""
): CommonState(), Parcelable