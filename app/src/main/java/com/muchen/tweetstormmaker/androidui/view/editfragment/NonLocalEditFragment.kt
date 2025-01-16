package com.muchen.tweetstormmaker.androidui.view.editfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.muchen.tweetstormandroid.R
import com.muchen.tweetstormandroid.databinding.FragmentEditNonLocalBinding
import com.muchen.tweetstormmaker.interfaceadapter.model.Draft

class NonLocalEditFragment : BaseEditFragment() {

    protected override val timeCreated = lazy { args.timeCreated }

    private val args: NonLocalEditFragmentArgs by navArgs()

    private lateinit var binding: FragmentEditNonLocalBinding

    private var nonLocalDraft: Draft? = null

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
            btnUnsend.setOnClickListener { unsend() }
            btnDiscardNonLocal.setOnClickListener{ discard() }
            draftsViewModel.getDraftByTimeCreated(timeCreated.value).observe(viewLifecycleOwner) {
                if (it != null) {
                    textViewDraft.setText(it.content)
                    nonLocalDraft = it
                }
            }
        }
    }

    private fun unsend() {
        val keyString = getString(R.string.preference_key_save_to_draft_after_unsending_fully_sent)
        val keepInDraft = PreferenceManager.getDefaultSharedPreferences(requireActivity())
                .getBoolean(keyString, false)
        twitterApiViewModel.unsendTweetstorm(nonLocalDraft!!, keepInDraft)
    }
}