package com.nominalista.expenses.expensedetail.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.nominalista.expenses.R
import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.common.presentation.BaseActivity
import kotlinx.android.synthetic.main.activity_expense_detail.*

class ExpenseDetailActivity: BaseActivity() {

    override var animationKind = ANIMATION_SLIDE_FROM_RIGHT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_detail)
        setSupportActionBar(findViewById(R.id.toolbar))
        setGraph()
    }

    private fun setGraph() {
        val navController = (fragment_navigation_host as NavHostFragment).navController
        navController.setGraph(makeGraph(navController), makeStartDestinationBundle())
    }

    private fun makeGraph(navController: NavController): NavGraph {
        val graph = navController.navInflater.inflate(R.navigation.navigation_expense_detail)
        graph.startDestination = R.id.fragment_expense_detail
        return graph
    }

    private fun makeStartDestinationBundle(): Bundle {
        val expense = intent.getParcelableExtra<Expense>(EXTRA_EXPENSE)
        return ExpenseDetailFragmentArgs.Builder(expense).build().toBundle()
    }

    companion object {

        private const val EXTRA_EXPENSE = "expense"

        fun start(context: Context, expense: Expense) {
            val intent = Intent(context, ExpenseDetailActivity::class.java).apply {
                putExtra(EXTRA_EXPENSE, expense)
            }
            context.startActivity(intent)
        }
    }
}