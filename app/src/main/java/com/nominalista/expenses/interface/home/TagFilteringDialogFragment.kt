package com.nominalista.expenses.`interface`.home

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.nominalista.expenses.R
import com.nominalista.expenses.data.Tag

private const val ARGUMENT_TAGS = "com.nominalista.expenses.ARGUMENT_TAGS"

class TagFilteringDialogFragment : DialogFragment(), DialogInterface.OnMultiChoiceClickListener {

    companion object {

        fun newInstance(tags: List<Tag>): TagFilteringDialogFragment {
            val arguments = Bundle()
            arguments.putParcelableArrayList(ARGUMENT_TAGS, ArrayList(tags))

            val fragment = TagFilteringDialogFragment()
            fragment.arguments = arguments
            return fragment
        }
    }

    var tagsFiltered: ((TagFilter) -> Unit)? = null

    private lateinit var tags: List<Tag>
    private val filter = TagFilter()

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tags = arguments?.getParcelableArrayList(ARGUMENT_TAGS) ?: emptyList()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity())
                .setTitle(R.string.filter_tags)
                .setMultiChoiceItems(getItems(), getCheckedItems(), this)
                .setPositiveButton(R.string.ok, { _, _ -> tagsFiltered?.invoke(filter) })
                .setNegativeButton(R.string.cancel, { _, _ -> })
                .create()
    }

    private fun getItems() = Array(tags.size, { tags[it].name })

    private fun getCheckedItems() = BooleanArray(tags.size, { false })

    override fun onClick(dialog: DialogInterface?, which: Int, isChecked: Boolean) {
        val tag = tags[which]
        if (isChecked) filter.add(tag)
        else filter.remove(tag)
    }
}