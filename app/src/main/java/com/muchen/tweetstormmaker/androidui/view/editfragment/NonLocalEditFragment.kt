package com.muchen.tweetstormmaker.androidui.view.editfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.muchen.tweetstormmaker.R
import com.muchen.tweetstormmaker.androidui.model.Draft
import com.muchen.tweetstormmaker.databinding.FragmentEditNonLocalBinding

class NonLocalEditFragment : BaseEditFragment() {

    protected override val timeCreated = lazy { args.timeCreated }

    private val args: NonLocalEditFragmentArgs by navArgs()

    private lateinit var binding: FragmentEditNonLocalBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // perform field injection
        binding = FragmentEditNonLocalBinding.inflate(layoutInflater, container, false)
        setupBinding()
        return binding.root
    }

    private fun setupBinding() {
        binding.apply {
            lifecycleOwner = this@NonLocalEditFragment
            twitterApiViewModel = this@NonLocalEditFragment.twitterApiViewModel
            hasInternetAccess = this@NonLocalEditFragment.hasInternetAccess
            draft = this@NonLocalEditFragment.draftsViewModel.getDraftByTimeCreated(timeCreated.value)
            btnUnsend.setOnClickListener { unsend() }
            btnDiscardNonLocal.setOnClickListener{ discard() }
        }
    }

    private fun unsend() {
        val keyString = getString(R.string.preference_key_save_to_draft_after_unsending_fully_sent)
        val keepInDraft = PreferenceManager.getDefaultSharedPreferences(requireActivity())
                .getBoolean(keyString, false)
        twitterApiViewModel.unsendTweetstorm(binding.draft!!.value!!, keepInDraft)
    }
}