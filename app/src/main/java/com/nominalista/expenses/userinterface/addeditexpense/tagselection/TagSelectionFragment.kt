package com.nominalista.expenses.userinterface.addeditexpense.tagselection

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nominalista.expenses.R
import com.nominalista.expenses.infrastructure.extensions.application
import com.nominalista.expenses.infrastructure.extensions.plusAssign
import io.reactivex.disposables.CompositeDisposable

class TagSelectionFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TagSelectionAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var model: TagSelectionFragmentModel

    private val compositeDisposable = CompositeDisposable()

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
        setupModel()
        bindModel()
    }

    private fun bindWidgets(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
    }

    private fun setupActionBar() {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar ?: return
        actionBar.title = getString(R.string.select_tags)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_active_light_24dp)
        setHasOptionsMenu(true)
    }

    private fun setupRecyclerView() {
        adapter =
            TagSelectionAdapter()
        layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
    }

    private fun setupModel() {
        val factory =
            TagSelectionFragmentModel.Factory(
                requireContext().application
            )
        model = ViewModelProviders.of(this, factory).get(TagSelectionFragmentModel::class.java)
    }

    private fun bindModel() {
        compositeDisposable += model.itemModels.toObservable().subscribe(adapter::submitList)
        compositeDisposable += model.showNewTagDialog
                .toObservable()
                .subscribe { showNewTagDialog() }
    }

    private fun showNewTagDialog() {
        val dialogFragment =
            NewTagDialogFragment.newInstance()
        dialogFragment.tagCreated = { model.createTag(it) }
        dialogFragment.show(requireFragmentManager(), "NewTagDialogFragment")
    }

    // Lifecycle end

    override fun onDestroyView() {
        super.onDestroyView()
        unbindModel()
    }

    private fun unbindModel() {
        compositeDisposable.clear()
    }

    // Options

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_tag_selection, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
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
        requireActivity().onBackPressed()
        return true
    }
}