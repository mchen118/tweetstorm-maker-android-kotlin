package com.muchen.tweetstormmaker.androidui.view.listfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.muchen.tweetstormmaker.androidui.adatper.DraftListAdapter
import com.muchen.tweetstormandroid.databinding.FragmentListSentBinding

class SentListFragment : BaseListFragment() {

    override val listLiveData = lazy { draftsViewModel.sentDrafts }

    override val navigateToEditFragment = { navController: NavController, timeCreated: Long ->
        navController.navigate(SentListFragmentDirections
            .actionSentListFragmentToNonLocalEditFragment(timeCreated))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // perform field injection
        performInjection()
        binding = FragmentListSentBinding.inflate(layoutInflater, container, false)
        setupBinding()
        subscribeUi()
        return binding.root
    }

    private fun setupBinding() {
        (binding as FragmentListSentBinding).apply {
            rvDraftList.adapter = this@SentListFragment.adapter
            btnDiscardAllSentDrafts.setOnClickListener { discardAllSentDrafts() }
        }
    }

    private fun subscribeUi() {
        draftsViewModel.sentDrafts.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            // (binding as FragmentListSentBinding).listSize = list.size
        }
    }

    private fun discardAllSentDrafts() {
        val sentDrafts = draftsViewModel.sentDrafts.value
        if (sentDrafts.isNullOrEmpty()) {
            return
        } else {
            for (draft in sentDrafts) {
                draftsViewModel.deleteDraftByTimeCreated(draft.timeCreated)
            }
        }
    }
}