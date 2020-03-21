package com.nominalista.expenses.settings.presentation

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.nominalista.expenses.R
import com.nominalista.expenses.common.presentation.BaseActivity

class RuleActivity : BaseActivity() {

    override var animationKind = ANIMATION_SLIDE_FROM_RIGHT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rule)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    companion object {
        fun start(fragment: Fragment, requestCode: Int) {
            val intent = Intent(fragment.requireContext(), RuleActivity::class.java)
            fragment.startActivityForResult(intent, requestCode)
        }
    }
}