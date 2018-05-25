package com.nominalista.expenses.infrastructure.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun Activity.showKeyboard(view: View) {
    view.requestFocus()
    val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    manager.showSoftInput(view, 0)
}

fun Activity.hideKeyboard() {
    val view = currentFocus ?: View(this)
    val manager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    manager.hideSoftInputFromWindow(view.windowToken, 0)
}