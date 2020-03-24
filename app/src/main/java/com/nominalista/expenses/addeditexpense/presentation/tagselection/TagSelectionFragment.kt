package com.nominalista.expenses.addeditexpense.presentation.tagselection

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nominalista.expenses.R
import com.nominalista.expenses.util.extensions.application
import com.nominalista.expenses.util.extensions.plusAssign
import com.nominalista.expenses.addeditexpense.presentation.AddEditExpenseActivityModel
import io.reactivex.disposables.CompositeDisposable

class TagSelectionFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TagSelectionAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var activityModel: AddEditExpenseActivityModel
    private lateinit var model: TagSelectionFragmentModel

    private val disposables = CompositeDisposable()

    // Lifecycle start

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_tag_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindWidgets(view)
        setupActionBar()
        setupRecyclerView()
        setupModels()
        bindModel()
    }

    private fun bindWidgets(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
    }

    private fun setupActionBar() {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar ?: return
        actionBar.title = getString(R.string.select_tags)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp)
        setHasOptionsMenu(true)
    }

    private fun setupRecyclerView() {
        adapter = TagSelectionAdapter()
        layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
    }

    private fun setupModels() {
        activityModel = ViewModelProviders.of(requireActivity())
            .get(AddEditExpenseActivityModel::class.java)

        val factory = TagSelectionFragmentModel.Factory(requireContext().application)
        model = ViewModelProviders.of(this, factory).get(TagSelectionFragmentModel::class.java)
    }

    private fun bindModel() {
        disposables += model.itemModels.toObservable().subscribe(adapter::submitList)
        disposables += model.showNewTagDialog
            .toObservable()
            .subscribe { showNewTagDialog() }
        disposables += model.delegateSelectedTags
            .toObservable()
            .subscribe { activityModel.selectTags(it) }
        disposables += model.finish
            .toObservable()
            .subscribe { requireActivity().onBackPressed() }
    }

    private fun showNewTagDialog() {
        val dialogFragment = NewTagDialogFragment.newInstance()
        dialogFragment.tagCreated = { model.createTag(it) }
        dialogFragment.show(requireFragmentManager(), "NewTagDialogFragment")
    }

    // Lifecycle end

    override fun onDestroyView() {
        super.onDestroyView()
        clearDisposables()
    }

    private fun clearDisposables() {
        disposables.clear()
    }

    // Options

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_tag_selection, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> backSelected()
            R.id.confirm -> confirmSelected()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun backSelected(): Boolean {
        requireActivity().onBackPressed()
        return true
    }

    private fun confirmSelected(): Boolean {
        model.confirm()
        return true
    }
}