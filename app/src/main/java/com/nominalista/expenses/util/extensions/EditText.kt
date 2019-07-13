package com.nominalista.expenses.util.extensions

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

fun EditText.afterTextChanged(function: (Editable?) -> Unit) {
    addTextChangedListener(object : TextWatcher {

        override fun afterTextChanged(editable: Editable?) {
            function(editable)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Ignore
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Ignore
        }

    })
}