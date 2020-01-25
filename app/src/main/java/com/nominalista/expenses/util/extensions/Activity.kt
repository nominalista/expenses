package com.nominalista.expenses.util.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.nominalista.expenses.Application

fun Activity.requireApplication(): Application = (applicationContext as Application)

fun Activity.showKeyboard(view: View, delay: Long = 0) {
    view.postDelayed({
        view.requestFocus()
        val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.showSoftInput(view, 0)
    }, delay)
}

fun Activity.hideKeyboard(view: View? = null) {
    val windowToken = view?.windowToken ?: getCurrentFocusOrPlaceholder().windowToken
    val manager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    manager.hideSoftInputFromWindow(windowToken, 0)
}

private fun Activity.getCurrentFocusOrPlaceholder() = currentFocus ?: View(this)

/**
 * Shows or hides keyboard. It is especially useful for DialogFragments where hideKeyboard() seems
 * to be not working correctly.
 */
fun Activity.toggleKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(0, 0)
}

/**
 * Try to start activity if no sure that this activity exits.
 */
fun Activity.startActivitySafely(intent: Intent) {
    if (intent.resolveActivity(packageManager) != null) startActivity(intent)
}