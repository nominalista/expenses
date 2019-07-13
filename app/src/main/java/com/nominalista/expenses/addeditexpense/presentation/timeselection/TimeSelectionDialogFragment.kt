package com.nominalista.expenses.addeditexpense.presentation.timeselection

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*


class TimeSelectionDialogFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    var timeSelected: ((Int, Int) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val is24HourView = DateFormat.is24HourFormat(requireContext())
        return TimePickerDialog(requireContext(), this, hourOfDay, minute, is24HourView)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        timeSelected?.invoke(hourOfDay, minute)
    }
}