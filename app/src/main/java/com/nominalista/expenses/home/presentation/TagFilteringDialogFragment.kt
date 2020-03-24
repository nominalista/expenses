package com.nominalista.expenses.home.presentation

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nominalista.expenses.R
import com.nominalista.expenses.data.model.Tag

class TagFilteringDialogFragment : DialogFragment(), DialogInterface.OnMultiChoiceClickListener {

    var tagsFiltered: ((TagFilter) -> Unit)? = null

    private lateinit var tags: List<Tag>

    private val filter = TagFilter()

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tags = arguments?.getParcelableArrayList(ARGUMENT_TAGS) ?: emptyList()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.filter_tags)
            .setMultiChoiceItems(getItems(), getCheckedItems(), this)
            .setPositiveButton(R.string.ok) { _, _ -> tagsFiltered?.invoke(filter) }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .create()
            .apply { setOnShowListener { enableOrDisablePositiveButton() } }
    }



    private fun getItems() = Array(tags.size) { tags[it].name }

    private fun getCheckedItems() = BooleanArray(tags.size) { false }

    private fun enableOrDisablePositiveButton() {
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !filter.isEmpty
    }

    override fun onClick(dialog: DialogInterface?, which: Int, isChecked: Boolean) {
        tags[which].let {
            if (isChecked) {
                filter.add(it)
            } else {
                filter.remove(it)
            }
        }

        enableOrDisablePositiveButton()
    }

    companion object {

        private const val ARGUMENT_TAGS = "com.nominalista.expenses.ARGUMENT_TAGS"

        fun newInstance(tags: List<Tag>): TagFilteringDialogFragment {
            val arguments = Bundle().apply {
                putParcelableArrayList(ARGUMENT_TAGS, ArrayList(tags))
            }

            return TagFilteringDialogFragment().apply { this.arguments = arguments }
        }
    }
}