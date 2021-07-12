package com.radziejewskig.todo.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task (
    @DocumentId
    var id: String = "",
    var title: String = "",
    var description: String? = null,
    var iconUrl: String? = null,
    var completed: Boolean = false,
    @ServerTimestamp
    var createdAt: Timestamp? = null,
): Parcelable
