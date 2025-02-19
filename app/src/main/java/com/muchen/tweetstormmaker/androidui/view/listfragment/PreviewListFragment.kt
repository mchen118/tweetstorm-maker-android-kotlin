package com.muchen.tweetstormmaker.androidui.view.listfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.muchen.tweetstormandroid.R
import com.muchen.tweetstormandroid.databinding.FragmentListPreviewBinding
import com.muchen.tweetstormmaker.androidui.AndroidUIConstants.DEFAULT_NUMBERING_TWEETS_VALUE
import com.muchen.tweetstormmaker.androidui.adatper.PreviewListAdapter
import com.muchen.tweetstormmaker.androidui.view.MainActivity
import com.muchen.tweetstormmaker.androidui.view.editfragment.LocalEditFragment.Companion.TAG
import com.muchen.tweetstormmaker.interfaceadapter.InterfaceAdapterConstants.DEFAULT_TWEET_NUMBER_PREFIX
import com.muchen.tweetstormmaker.interfaceadapter.InterfaceAdapterConstants.DEFAULT_TWITTER_HANDLE_POSTFIX
import com.muchen.tweetstormmaker.interfaceadapter.TextToTweetsProcessor
import com.muchen.tweetstormmaker.interfaceadapter.model.DraftContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreviewListFragment : Fragment() {

    private val args: PreviewListFragmentArgs by navArgs()

    protected val draftsViewModel by lazy {
        (requireActivity() as MainActivity).draftsViewModel
    }

    protected val twitterApiViewModel by lazy {
        (requireActivity() as MainActivity).twitterApiViewModel
    }

    private lateinit var binding: FragmentListPreviewBinding

    private lateinit var adapter: PreviewListAdapter

    private lateinit var content: String

    private var numberingTweets: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = PreviewListAdapter()
        val keyString = getString(R.string.preference_key_numbering_tweets)
        numberingTweets = PreferenceManager.getDefaultSharedPreferences(requireActivity())
            .getBoolean(keyString, DEFAULT_NUMBERING_TWEETS_VALUE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListPreviewBinding.inflate(layoutInflater, container, false)
        binding.apply {
            rvPreviewList.adapter = adapter
            btnBack.setOnClickListener { back() }
            btnTweet.setOnClickListener { tweet() }
        }

        draftsViewModel.getDraftByTimeCreated(args.timeCreated).observe(viewLifecycleOwner) { draft ->
            content = draft!!.content
            lifecycleScope.launch(Dispatchers.Default) {
                val list = ArrayList<String>()
                val textToTweetsProcessor = TextToTweetsProcessor(
                    content,
                    "@${twitterApiViewModel.twitterUserAndTokens.value!!.screenName}",
                    DEFAULT_TWITTER_HANDLE_POSTFIX,
                    DEFAULT_TWEET_NUMBER_PREFIX,
                    numberingTweets,
                    twitterApiViewModel.TWEET_MAX_WEIGHTED_LENGTH,
                    twitterApiViewModel.SHORTENED_URL_LENGTH)
                while (textToTweetsProcessor.hasNextTweet()) {
                    list.add(textToTweetsProcessor.nextTweet())
                }
                withContext(Dispatchers.Main) {
                    adapter.submitList(list)
                }
            }
        }

        return binding.root
    }

    private fun back() {
        findNavController().popBackStack()
    }

    private fun tweet() {
        Log.d(TAG, "numberingTweets: $numberingTweets, " +
                "text: ${draftsViewModel.getDraftByTimeCreated(args.timeCreated).value}")
        twitterApiViewModel.sendTweetStorm(DraftContent(args.timeCreated, content), numberingTweets)
        findNavController().navigate(PreviewListFragmentDirections.
                actionPreviewListFragmentToLocalListFragment())
    }
}