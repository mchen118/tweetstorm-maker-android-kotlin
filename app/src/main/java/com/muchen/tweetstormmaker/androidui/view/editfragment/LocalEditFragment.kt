package com.muchen.tweetstormmaker.androidui.view.editfragment

import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.muchen.tweetstormmaker.R
import com.muchen.tweetstormmaker.androidui.AndroidUIConstants.DEFAULT_NUMBERING_TWEETS_VALUE
import com.muchen.tweetstormmaker.androidui.AndroidUIConstants.STYLE_EDIT_TEXT_DELAY_IN_MS
import com.muchen.tweetstormmaker.androidui.model.DraftContent
import com.muchen.tweetstormmaker.databinding.FragmentEditLocalBinding
import com.twitter.twittertext.Extractor
import kotlinx.coroutines.*
import java.util.*

class LocalEditFragment : BaseEditFragment() {

    protected override val timeCreated = lazy { args.timeCreated }

    private lateinit var binding: FragmentEditLocalBinding

    private val args: LocalEditFragmentArgs by navArgs()

    private val textWatcher = object : TextWatcher {

        var beforeString: String? = null
        var onString: String? = null
        var lastJob: Job? = null

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            beforeString = s.toString()
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onString = s.toString()
            Log.d(TAG, "textWatcher: before: $beforeString, on: $onString, replacement " +
                    "starts at position $start with $count characters")
        }

        override fun afterTextChanged(s: Editable?) {
            // guard against infinite loop
            if (beforeString != onString) {
                binding.editTextNotNullNorBlank = !onString.isNullOrBlank()
                lastJob?.cancel()
                lastJob = lifecycleScope.launch(Dispatchers.Main) {
                    delay(STYLE_EDIT_TEXT_DELAY_IN_MS)
                    // could cause infinite loop if not protected by "beforeString != onString"
                    stylizeTwitterEntitiesInText(onString)
                    if (!onString.isNullOrBlank()) {
                        this@LocalEditFragment.draftsViewModel.updateDraftContent(
                                DraftContent(timeCreated.value, onString!!))
                    }
                }
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // perform field injection
        binding = FragmentEditLocalBinding.inflate(layoutInflater, container, false)
        setupBinding()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (binding.editTextDraft.text.isNullOrBlank())
            draftsViewModel.deleteDraftByTimeCreated(timeCreated.value)
    }

    private fun setupBinding() {
        binding.apply {
            lifecycleOwner = this@LocalEditFragment
            twitterApiViewModel = this@LocalEditFragment.twitterApiViewModel
            hasInternetAccess = this@LocalEditFragment.hasInternetAccess
            draft = this@LocalEditFragment.draftsViewModel.getDraftByTimeCreated(timeCreated.value)
            btnTweet.setOnClickListener { tweet() }
            btnDiscardLocal.setOnClickListener { discard() }
            editTextDraft.addTextChangedListener(textWatcher)
        }
    }

    private fun tweet() {
        val keyString = getString(R.string.preference_key_numbering_tweets)
        val numberingTweets = PreferenceManager.getDefaultSharedPreferences(requireActivity())
                .getBoolean(keyString, DEFAULT_NUMBERING_TWEETS_VALUE)
        Log.d(TAG, "numberingTweets: $numberingTweets")
        twitterApiViewModel.sendTweetStorm(DraftContent(
                timeCreated.value, binding.editTextDraft.text.toString()), numberingTweets)
    }

    private fun stylizeTwitterEntitiesInText(text: String?) {
        if (!text.isNullOrBlank()) {
            val twitterEntityList = Extractor().extractEntitiesWithIndices(text)
            var spannableString: SpannableString? = null
            if (twitterEntityList.isNotEmpty()) {
                spannableString = SpannableString(text)
                for (entity in twitterEntityList) {
                    /* Extractor.Entity.getStart() returns the inclusive start index and
                       Extractor.Entity.getEnd() returns the exclusive end index. */
                    Log.d(TAG, "twitter entity: ${text.substring(entity.start, entity.end)}" +
                                ", start: ${entity.start}, end: ${entity.end}")
                    if (entity.type == Extractor.Entity.Type.URL) {
                        /* SpannableString.setSpan(...) follows the same indexing convention as
                           String.substring(int, int) */
                        spannableString.setSpan(UnderlineSpan(), entity.start, entity.end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    val color = ResourcesCompat.getColor(resources, R.color.twitterEntityTextColor,
                            requireActivity().theme)
                    spannableString.setSpan(ForegroundColorSpan(color), entity.start, entity.end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            binding.editTextDraft.apply {
                Log.d(TAG, "setText")
                val oldSelectionStart = selectionStart
                setText(spannableString?: text)
                /* Selection gets reset to [0, 0] with setText() call, so it needs to be restored.
                   Otherwise the cursor position would move. */
                setSelection(oldSelectionStart)
            }
        }
    }

    companion object {
        const val TAG = "LocalEditFragment"
    }
}