package com.radziejewskig.todo.extension

import android.content.Context
import android.os.Build
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes

fun Context.getColorFromRes(
    @ColorRes res: Int
) = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    getColor(res)
} else {
    resources.getColor(res)
}

@ColorInt
fun Context.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}