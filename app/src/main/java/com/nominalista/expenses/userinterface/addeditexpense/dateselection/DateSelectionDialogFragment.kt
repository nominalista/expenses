package com.nominalista.expenses.userinterface.addeditexpense.dateselection

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DateSelectionDialogFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    var dateSelected: ((Int, Int, Int) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(requireActivity(), this, year, month, dayOfMonth)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        // Selected month is from 0 to 11.
        // https://developer.android.com/reference/android/app/DatePickerDialog.OnDateSetListener
        dateSelected?.invoke(year, month + 1, dayOfMonth)
    }
}