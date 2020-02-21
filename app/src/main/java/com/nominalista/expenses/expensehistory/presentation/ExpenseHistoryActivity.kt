package com.nominalista.expenses.expensehistory.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.nominalista.expenses.R
import com.nominalista.expenses.common.presentation.BaseActivity

class ExpenseHistoryActivity : BaseActivity() {

    override var animationKind = ANIMATION_SLIDE_FROM_RIGHT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_history)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, ExpenseHistoryActivity::class.java)
            context.startActivity(intent)
        }
    }
}