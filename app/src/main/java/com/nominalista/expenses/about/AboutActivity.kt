package com.nominalista.expenses.about

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.nominalista.expenses.R
import com.nominalista.expenses.common.presentation.BaseActivity
import com.nominalista.expenses.util.extensions.application
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : BaseActivity() {
    override var animationKind = ANIMATION_SLIDE_FROM_RIGHT

    private lateinit var model: AboutActivityModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setupModel()
        setupActionBar()
    }

    private fun setupModel() {
        val factory = AboutActivityModel.Factory(applicationContext.application)
        model = ViewModelProviders.of(this, factory).get(AboutActivityModel::class.java)
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar)
    }

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, AboutActivity::class.java)
            context.startActivity(intent)
        }
    }
}