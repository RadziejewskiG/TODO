package com.radziejewskig.todo.extension

import android.widget.EditText

fun String?.trimNormalize() = this?.trim()?.replace("\\s+".toRegex(), " ")

fun EditText.setTextIfDiffers(text: String?) = if (
    (text.isNullOrEmpty() && this.text.isNullOrEmpty()) ||
    text == this.text?.toString()
) Unit else this.setText(text)
