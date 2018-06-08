package com.nominalista.expenses.`interface`

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.nominalista.expenses.R
import com.nominalista.expenses.data.Expense
import com.nominalista.expenses.data.Tag
import com.nominalista.expenses.`interface`.expensedetail.ExpenseDetailFragment
import com.nominalista.expenses.`interface`.home.HomeFragment
import com.nominalista.expenses.`interface`.newexpense.NewExpenseFragment
import com.nominalista.expenses.`interface`.settings.SettingsFragment
import com.nominalista.expenses.`interface`.tagselection.TagSelectionFragment

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindWidgets()
        setupToolbar()
        navigateToHome()
    }

    private fun bindWidgets() {
        toolbar = findViewById(R.id.toolbar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    fun navigateToExpenseDetail(expense: Expense) {
        replace(ExpenseDetailFragment.newInstance(expense), "ExpenseDetailFragment")
    }

    fun navigateToHome() {
        replace(HomeFragment.newInstance(), "HomeFragment")
    }

    fun navigateToNewExpense() {
        replace(NewExpenseFragment.newInstance(), "NewExpenseFragment")
    }

    fun navigateToSettings() {
        replace(SettingsFragment.newInstance(), "SettingsFragment")
    }

    fun navigateToTagSelection(tagsSelected: ((List<Tag>) -> Unit)? = null) {
        val fragment = TagSelectionFragment.newInstance()
        fragment.tagsSelected = tagsSelected
        replace(fragment, "TagSelectionFragment")
    }

    private fun replace(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.layout_container, fragment)
                .addToBackStack(tag)
                .commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            super.onBackPressed()
        } else {
            finish()
        }
    }

    // override fun onSupportNavigateUp() = findNavController(R.id.fragment_navigation_host).navigateUp()
}
