package com.nominalista.expenses.sms.rule


import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.R
import com.nominalista.expenses.data.model.Rule
import com.nominalista.expenses.util.extensions.afterTextChanged
import com.nominalista.expenses.util.extensions.application
import com.nominalista.expenses.util.extensions.showKeyboard
import com.nominalista.expenses.util.extensions.toggleKeyboard

class AddRuleFragment : Fragment() {
    private lateinit var saveButton: MenuItem
    private lateinit var model: RuleFragmentModel

    private var id = ""

    private lateinit var ruleEditText: EditText
    private val rule get() = ruleEditText.text.toString()

    private lateinit var firstSymbolEditText: EditText
    private val firstSymbol get() = firstSymbolEditText.text.toString()

    private lateinit var lastSymbolEditText: EditText
    private val lastSymbol get() = lastSymbolEditText.text.toString()

    private lateinit var decimalSeparatorEditText: EditText
    private val decimalSeparator get() = decimalSeparatorEditText.text.toString()

    private lateinit var groupSeparatorEditText: EditText
    private val groupSeparator get() = groupSeparatorEditText.text.toString()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_rule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupModels()
        setupActionBar()
        bindEditText(view)
        val rule = arguments?.let { AddRuleFragmentArgs.fromBundle(it).rule }
        rule?.id.let { id = it.orEmpty() }
        ruleEditText.setText(rule?.name)
        firstSymbolEditText.setText(rule?.firstSymbol)
        lastSymbolEditText.setText(rule?.lastSymbol)
        decimalSeparatorEditText.setText(rule?.decimalSeparator)
        groupSeparatorEditText.setText(rule?.groupSeparator)
        watchEditText()
        showKeyboard(ruleEditText)
    }

    private fun setupModels() {
        val factory = RuleFragmentModel.Factory(requireContext().application)
        model = ViewModelProvider(this, factory).get(RuleFragmentModel::class.java)
    }

    private fun setupActionBar() {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar ?: return
        actionBar.title = ""
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_24dp)
        setHasOptionsMenu(true)
    }

    private fun bindEditText(view: View) {
        ruleEditText = view.findViewById(R.id.rule_edit_text)
        firstSymbolEditText = view.findViewById(R.id.first_symbol_edit_text)
        lastSymbolEditText = view.findViewById(R.id.last_symbol_edit_text)
        decimalSeparatorEditText = view.findViewById(R.id.decimal_separator_edit_text)
        groupSeparatorEditText = view.findViewById(R.id.group_separator_edit_text)
    }

    private fun watchEditText() {
        ruleEditText.afterTextChanged { enableOrDisableEditText() }
        firstSymbolEditText.afterTextChanged { enableOrDisableEditText() }
        lastSymbolEditText.afterTextChanged { enableOrDisableEditText() }
        decimalSeparatorEditText.afterTextChanged { enableOrDisableEditText() }
        groupSeparatorEditText.afterTextChanged { enableOrDisableEditText() }
    }

    private fun enableOrDisableEditText() {
        saveButton.isEnabled = rule.isNotEmpty() && firstSymbol.isNotEmpty() && lastSymbol.isNotEmpty() && decimalSeparator.isNotEmpty() && groupSeparator.isNotEmpty()
    }

    // Options
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_new_expense, menu)
        saveButton = menu.getItem(0)
        enableOrDisableEditText()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> backSelected()
            R.id.save -> saveSelected()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun backSelected(): Boolean {
        toggleKeyboard()
        requireActivity().onBackPressed()
        return true
    }

    private fun saveSelected(): Boolean {
        save()
        return true
    }

    private fun save() {
        if (id.isNotEmpty()) {
            model.updateRule(Rule(id, rule, firstSymbol, lastSymbol, decimalSeparator, groupSeparator))
        } else {
            model.createRule(Rule(id, rule, firstSymbol, lastSymbol, decimalSeparator, groupSeparator))
        }
        backSelected()
    }
}
