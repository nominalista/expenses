package com.nominalista.expenses.settings.presentation

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nominalista.expenses.R
import com.nominalista.expenses.common.presentation.DarkMode

class DarkModeSelectionDialogFragment : DialogFragment(), DialogInterface.OnClickListener {

    var onThemeSelected: ((DarkMode) -> Unit)? = null

    private val themes by lazy { DarkMode.values() }

    private lateinit var currentDarkMode: DarkMode

    private var selectedDarkMode: DarkMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentDarkMode = arguments?.getString(ARGUMENT_CURRENT_DARK_MODE)
            ?.let { DarkMode.valueOf(it) } ?: DarkMode.SYSTEM_DEFAULT
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.dark_mode)
            .setSingleChoiceItems(getItems(), getCheckedItem(), this)
            .setPositiveButton(R.string.ok) { _, _ ->
                selectedDarkMode?.let { onThemeSelected?.invoke(it) }
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .create()
    }

    private fun getItems(): Array<String> {
        return themes.map { theme ->
            when (theme) {
                DarkMode.ON -> requireContext().getString(R.string.on)
                DarkMode.OFF -> requireContext().getString(R.string.off)
                DarkMode.SYSTEM_DEFAULT -> requireContext().getString(R.string.system_default)
            }
        }.toTypedArray()
    }

    private fun getCheckedItem(): Int = themes.indexOf(currentDarkMode)

    override fun onClick(dialog: DialogInterface, which: Int) {
        selectedDarkMode = themes[which]
    }

    companion object {
        const val TAG = "ThemeSelectionDialogFragment"

        private const val ARGUMENT_CURRENT_DARK_MODE =
            "com.nominalista.expenses.ARGUMENT_CURRENT_DARK_MODE"

        fun newInstance(currentDarkMode: DarkMode): DarkModeSelectionDialogFragment {
            val arguments = Bundle().apply {
                putString(ARGUMENT_CURRENT_DARK_MODE, currentDarkMode.name)
            }

            return DarkModeSelectionDialogFragment().apply { this.arguments = arguments }
        }
    }
}