package com.radziejewskig.todo.feature.takephoto

import android.os.Parcelable
import com.radziejewskig.todo.base.CommonEvent
import com.radziejewskig.todo.base.CommonState
import kotlinx.parcelize.Parcelize

sealed class TakePhotoEvent: CommonEvent {
    class ImageSaved(
        val imageUrl: String
    ): TakePhotoEvent()
}

@Parcelize
data class TakePhotoState(
    val currentPhotoPath: String = ""
): CommonState(), Parcelable