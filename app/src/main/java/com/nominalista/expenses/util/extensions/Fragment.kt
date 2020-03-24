package com.nominalista.expenses.util.extensions

import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.nominalista.expenses.Application

fun Fragment.requireApplication(): Application = requireActivity().requireApplication()

fun Fragment.requireAppCompatActivity(): AppCompatActivity = requireActivity() as AppCompatActivity

fun Fragment.getSupportActionBarOrNull(): ActionBar? = requireAppCompatActivity().supportActionBar

fun Fragment.showKeyboard(view: View, delay: Long = 0) {
    requireActivity().showKeyboard(view, delay)
}

fun Fragment.hideKeyboard(view: View? = null) {
    requireActivity().hideKeyboard(view)
}

fun Fragment.toggleKeyboard() {
    requireActivity().toggleKeyboard()
}