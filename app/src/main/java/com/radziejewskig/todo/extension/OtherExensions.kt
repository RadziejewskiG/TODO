package com.radziejewskig.todo.extension

import android.content.Context
import android.os.Build
import androidx.annotation.ColorRes

fun Context.getColorFromRes(
    @ColorRes res: Int
) = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    getColor(res)
} else {
    resources.getColor(res)
}