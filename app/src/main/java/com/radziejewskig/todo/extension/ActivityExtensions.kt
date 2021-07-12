package com.radziejewskig.todo.extension

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.radziejewskig.todo.base.BaseActivity

fun BaseActivity.hideKeyboard() {
    binding.root.let { dummyViewToCatchFocus ->
        val imm: InputMethodManager = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        dummyViewToCatchFocus.requestFocus()
        imm.hideSoftInputFromWindow(dummyViewToCatchFocus.windowToken, 0)
    }
}

fun AppCompatActivity.showKeyboard(view: View) {
    val imm: InputMethodManager = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view,0)
}

fun BaseActivity.setLightStatusBarAppearance(isLight: Boolean) {
    viewModel.isStatusBarLight.value = isLight
}
