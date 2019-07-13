package com.nominalista.expenses.home.presentation

import android.os.Bundle
import com.nominalista.expenses.R
import com.nominalista.expenses.common.presentation.BaseActivity

class HomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(findViewById(R.id.toolbar))
    }
}