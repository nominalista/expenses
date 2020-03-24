package com.nominalista.expenses.sms.rule


import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nominalista.expenses.R
import com.nominalista.expenses.data.model.Format
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

    private lateinit var formatDropdownMenu: AutoCompleteTextView
    private var format: Format? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_rule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupModels()
        setupActionBar()
        bindItems(view)
        val formats = listOf(Format("1,000.00", groupSeparator = ",", decimalSeparator = "."),
                Format("1.000,00", groupSeparator = ".", decimalSeparator = ","),
                Format("1 000.00", groupSeparator = " ", decimalSeparator = "."),
                Format("1 000,00", groupSeparator = " ", decimalSeparator = ","))
        populateDropdownMenu(formats)
        val rule = arguments?.let { AddRuleFragmentArgs.fromBundle(it).rule }
        rule?.let { fillValues(it, formats) }

        watchEditText()
        showKeyboard(ruleEditText)
    }

    private fun fillValues(rule: Rule, formats: List<Format>) {
        id = rule.id
        ruleEditText.setText(rule.keywords.reduce { acc, s ->"$acc $s"  })
        firstSymbolEditText.setText(rule.firstSymbol)
        try {
            val pFormat = formats.first { it.decimalSeparator == rule.decimalSeparator && it.groupSeparator == rule.groupSeparator }
            formatDropdownMenu.setText(pFormat.hint, false)
            format = pFormat
        } catch (e: NoSuchElementException) { }
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

    private fun bindItems(view: View) {
        ruleEditText = view.findViewById(R.id.rule_edit_text)
        firstSymbolEditText = view.findViewById(R.id.first_symbol_edit_text)
        formatDropdownMenu = view.findViewById(R.id.dropdown_menu)
    }

    private fun populateDropdownMenu(formats: List<Format>) {
        val formatsArray = formats.map { it.hint }
        val adapter: ArrayAdapter<String> = ArrayAdapter(context, R.layout.dropdown_menu_popup_item, formatsArray)
        formatDropdownMenu.setAdapter(adapter)
        formatDropdownMenu.keyListener = null
        formatDropdownMenu.onItemClickListener = (AdapterView.OnItemClickListener() { _: AdapterView<*>, _: View, position: Int, _: Long ->
            format = formats[position]
            enableOrDisableEditText()
        })
    }

    private fun watchEditText() {
        ruleEditText.afterTextChanged { enableOrDisableEditText() }
        firstSymbolEditText.afterTextChanged { enableOrDisableEditText() }
    }

    private fun enableOrDisableEditText() {
        saveButton.isEnabled = rule.isNotEmpty() && firstSymbol.isNotEmpty() && format != null
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
        val keywords = rule.split("\n")
        if (id.isNotEmpty()) {
            model.updateRule(Rule(id, keywords, firstSymbol, "lastSymbol", format!!.decimalSeparator, format!!.groupSeparator))
        } else {
            model.createRule(Rule(id, keywords, firstSymbol, "lastSymbol", format!!.decimalSeparator, format!!.groupSeparator))
        }
        backSelected()
    }
}
