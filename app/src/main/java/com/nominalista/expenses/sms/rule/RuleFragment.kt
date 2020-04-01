package com.nominalista.expenses.sms.rule


import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.nominalista.expenses.R
import com.nominalista.expenses.data.model.Rule
import com.nominalista.expenses.settings.presentation.SettingsFragment
import com.nominalista.expenses.util.extensions.application
import com.nominalista.expenses.util.extensions.plusAssign
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_rule.*

class RuleFragment : Fragment() {

    private lateinit var adapter: RuleAdapter
    private lateinit var model: RuleFragmentModel

    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_rule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        setupRecyclerView()
        setupModels()
        bindModel()
    }

    private fun setupActionBar() {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar ?: return
        actionBar.title = getString(R.string.rules)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp)
        setHasOptionsMenu(true)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> backSelected()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        adapter = RuleAdapter()
        ruleRecyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(requireContext())
        ruleRecyclerView.layoutManager = layoutManager
    }

    private fun setupModels() {
        val factory = RuleFragmentModel.Factory(requireContext().application)
        model = ViewModelProvider(this, factory).get(RuleFragmentModel::class.java)
    }

    private fun bindModel() {
        disposables += model.itemModels.toObservable().subscribe(adapter::submitList)
        disposables += model.showNewRule.toObservable().subscribe { showEditRule(null) }
        disposables += model.showEditRule.toObservable().subscribe { showEditRule(it) }
        disposables += model.showUserRuleDialog.toObservable().subscribe { showUseRuleDialog(it) }
        disposables += model.finish.toObservable().subscribe {
            requireActivity().setResult(SettingsFragment.Companion.CLOSE)
            requireActivity().finish()
        }
    }

    private fun showEditRule(rule: Rule?) {
        val action = RuleFragmentDirections.actionRuleFragmentToAddRuleFragment()
        action.rule = rule
        val navController = NavHostFragment.findNavController(this)
        if (navController.currentDestination?.id == R.id.ruleFragment) {
            navController.navigate(action)
        }
    }

    private fun showUseRuleDialog(rule: Rule) {
        val dialogFragment = UseRuleDialogFragment.newInstance()
        activity?.let {
            dialogFragment.useRule = { message -> model.useRule(rule, message, it.applicationContext) }
        }
        dialogFragment.show(requireFragmentManager(), "UseRuleDialog")
    }

    // Options

    private fun backSelected(): Boolean {
        requireActivity().setResult(Activity.RESULT_OK)
        requireActivity().finish()
        return true
    }


}
