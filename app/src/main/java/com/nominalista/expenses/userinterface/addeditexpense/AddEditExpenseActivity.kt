package com.nominalista.expenses.userinterface.addeditexpense

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.nominalista.expenses.R
import com.nominalista.expenses.userinterface.common.BaseActivity

class AddEditExpenseActivity : BaseActivity() {

    override var animationKind = ANIMATION_SLIDE_FROM_BOTTOM

    private lateinit var model: AddEditExpenseActivityModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_expense)
        setSupportActionBar(findViewById(R.id.toolbar))
        initializeModel()
    }

    private fun initializeModel() {
        model = ViewModelProviders.of(this).get(AddEditExpenseActivityModel::class.java)
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AddEditExpenseActivity::class.java)
            context.startActivity(intent)
        }
    }
}