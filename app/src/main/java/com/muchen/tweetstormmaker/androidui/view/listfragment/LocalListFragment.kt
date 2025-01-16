package com.muchen.tweetstormmaker.androidui.view.listfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.muchen.tweetstormmaker.androidui.adatper.DraftListAdapter
import com.muchen.tweetstormmaker.interfaceadapter.model.Draft
import com.muchen.tweetstormandroid.databinding.FragmentListLocalBinding

class LocalListFragment : BaseListFragment() {

    override val listLiveData = lazy { draftsViewModel.localDrafts }

    override val navigateToEditFragment = { navController: NavController, timeCreated: Long ->
        navController.navigate(LocalListFragmentDirections
            .actionLocalListFragmentToLocalEditFragment(timeCreated, false))
    }

    private val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val time = (viewHolder as DraftListAdapter.DraftViewHolder).draft.timeCreated
            draftsViewModel.deleteDraftByTimeCreated(time)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // perform field injection
        performInjection()
        binding = FragmentListLocalBinding.inflate(layoutInflater, container, false)
        setupBinding()
        subscribeUi()
        return binding.root
    }

    private fun setupBinding() {
        (binding as FragmentListLocalBinding).apply {
            rvDraftList.adapter = this@LocalListFragment.adapter
            ItemTouchHelper(itemTouchCallback).attachToRecyclerView(rvDraftList)
            btnCompose.setOnClickListener { compose() }
        }
    }

    private fun subscribeUi() {
        draftsViewModel.localDrafts.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            if (list.isEmpty()) {
                (binding as FragmentListLocalBinding).textViewEmptyList.visibility = View.VISIBLE
            } else {
                (binding as FragmentListLocalBinding).textViewEmptyList.visibility = View.INVISIBLE
            }
        }
    }

    private fun compose() {
        val newTimeCreated = System.currentTimeMillis()
        draftsViewModel.insertDraft(Draft(newTimeCreated))
        findNavController().navigate(LocalListFragmentDirections.
                actionLocalListFragmentToLocalEditFragment(newTimeCreated, true))
    }

    @VisibleForTesting
    fun deleteAllDrafts() {
        draftsViewModel.deleteAllDrafts()
    }

    @VisibleForTesting
    fun insertDraft(draft: Draft) {
        draftsViewModel.insertDraft(draft)
    }

    companion object {
        val TAG = "LocalListFragment"
    }
}