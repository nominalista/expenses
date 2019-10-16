package com.nominalista.expenses.addeditexpense.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.nominalista.expenses.R
import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.common.presentation.BaseActivity
import kotlinx.android.synthetic.main.activity_add_edit_expense.*

class AddEditExpenseActivity : BaseActivity() {

    override var animationKind = ANIMATION_SLIDE_FROM_BOTTOM

    private lateinit var model: AddEditExpenseActivityModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_expense)
        setSupportActionBar(findViewById(R.id.toolbar))
        setGraph()
        initializeModel()
    }

    private fun setGraph() {
        val navController = (fragment_navigation_host as NavHostFragment).navController
        navController.setGraph(makeGraph(navController), makeStartDestinationBundle())
    }

    private fun makeGraph(navController: NavController): NavGraph {
        val graph = navController.navInflater.inflate(R.navigation.navigation_add_edit_expense)
        graph.startDestination = R.id.fragment_add_edit_expense
        return graph
    }

    private fun makeStartDestinationBundle(): Bundle {
        val expense = intent.getParcelableExtra<Expense>(EXTRA_EXPENSE)
        return AddEditExpenseFragmentArgs.Builder(expense).build().toBundle()
    }

    private fun initializeModel() {
        model = ViewModelProviders.of(this).get(AddEditExpenseActivityModel::class.java)
    }

    companion object {

        private const val EXTRA_EXPENSE = "expense"

        fun start(context: Context, expense: Expense?) {
            val intent = Intent(context, AddEditExpenseActivity::class.java).apply {
                expense?.let { putExtra(EXTRA_EXPENSE, it) }
            }
            context.startActivity(intent)
        }
    }
}