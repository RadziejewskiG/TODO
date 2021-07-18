package com.radziejewskig.todo.extension

import android.widget.EditText

fun String?.trimNormalize() = this?.trim()?.replace("\\s+".toRegex(), " ")

fun EditText.setTextIfDiffers(t: String?) = if (
    (t.isNullOrEmpty() && text.isNullOrEmpty()) ||
    t == text?.toString()
) Unit else this.setText(t)