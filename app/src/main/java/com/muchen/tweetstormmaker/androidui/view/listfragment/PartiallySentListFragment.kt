package com.muchen.tweetstormmaker.androidui.view.listfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import com.muchen.tweetstormmaker.interfaceadapter.model.Draft
import com.muchen.tweetstormmaker.androidui.view.MainActivity
import com.muchen.tweetstormandroid.databinding.FragmentListPartiallySentBinding

class PartiallySentListFragment : BaseListFragment() {

    override val listLiveData = lazy { draftsViewModel.partiallySentDrafts }

    override val navigateToEditFragment = { navController: NavController, timeCreated: Long ->
        navController.navigate(PartiallySentListFragmentDirections
            .actionPartiallySentListFragmentToNonLocalEditFragment(timeCreated))
    }

    private val twitterApiViewModel by lazy {
        (requireActivity() as MainActivity).twitterApiViewModel
    }

    private val hasInternetAccess by lazy {
        (requireActivity() as MainActivity).hasInternetAccess
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // perform field injection
        performInjection()
        binding = FragmentListPartiallySentBinding.inflate(layoutInflater, container, false)
        setupBinding()
        subscribeUi()
        return binding.root
    }

    private fun setupBinding() {
        (binding as FragmentListPartiallySentBinding).apply {
            rvDraftList.adapter = this@PartiallySentListFragment.adapter
//            twitterApiViewModel = this@PartiallySentListFragment.twitterApiViewModel
//            hasInternetAccess = this@PartiallySentListFragment.hasInternetAccess
            btnRecallAllPartiallySentTweetstorms.setOnClickListener { unsendAllPartialTweetstorms() }
        }
    }

    private fun subscribeUi() {
        draftsViewModel.partiallySentDrafts.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            // (binding as FragmentListPartiallySentBinding).listSize = list.size
        }
    }

    private fun unsendAllPartialTweetstorms() {
        twitterApiViewModel.unsendTweetstorms(draftsViewModel.partiallySentDrafts.value
                as List<Draft>)
    }
}