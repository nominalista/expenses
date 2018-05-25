package com.nominalista.expenses.ui.common.userselection

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.nominalista.expenses.R
import com.nominalista.expenses.model.User
import java.util.*

class UserSelectionDialogFragment: DialogFragment(), DialogInterface.OnClickListener {

    companion object {

        fun newInstance(users: List<User>): UserSelectionDialogFragment {
            val bundle = Bundle()
            bundle.putParcelableArrayList(EXTRA_USERS, ArrayList(users))
            val fragment = UserSelectionDialogFragment()
            fragment.arguments = bundle
            return fragment
        }

        const val EXTRA_USERS = "com.nominalista.expenses.EXTRA_USERS"
    }

    var onUserSelected: ((User) -> Unit)? = null

    private lateinit var users: List<User>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setupUsers()
        return AlertDialog.Builder(requireActivity())
                .setTitle(R.string.ui_common_user_selection_select_user)
                .setItems(createItems(), this)
                .setNegativeButton(R.string.cancel, { _, _ -> })
                .create()
    }

    private fun setupUsers() {
        users = arguments?.getParcelableArrayList(EXTRA_USERS) ?: emptyList()
    }

    private fun createItems(): Array<String> {
        return users.map { user -> user.name }.toTypedArray()
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        onUserSelected?.invoke(users[which])
    }
}