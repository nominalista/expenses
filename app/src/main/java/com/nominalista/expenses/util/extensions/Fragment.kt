package com.nominalista.expenses.util.extensions

import android.view.View
import androidx.fragment.app.Fragment

fun Fragment.showKeyboard(view: View, delay: Long = 0) {
    requireActivity().showKeyboard(view, delay)
}

fun Fragment.hideKeyboard() {
    requireActivity().hideKeyboard()
}